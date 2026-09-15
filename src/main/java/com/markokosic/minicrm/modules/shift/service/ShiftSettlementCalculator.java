package com.markokosic.minicrm.modules.shift.service;

import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.remuneration.IRemunerationCalculator;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import com.markokosic.minicrm.modules.shift.ShiftSettlementMapper;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import com.markokosic.minicrm.modules.shift.model.ShiftSettlement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;

@Component
@RequiredArgsConstructor
public class ShiftSettlementCalculator {

	private final List<IRemunerationCalculator> calculators;
	private final ShiftSettlementMapper shiftSettlementMapper;

	public ShiftSettlement calculate(Shift shift) {
		Driver driver = shift.getDriver();
		if (driver == null) {
			throw new BadRequestException("domain.shift.driver_required");
		}

		BigDecimal totalDriverRemuneration = BigDecimal.ZERO;
		BigDecimal totalCompanyRemuneration = BigDecimal.ZERO;
		BigDecimal totalRevenue = BigDecimal.ZERO;

		List<ShiftRevenueEntry> entries = shift.getRevenues() != null ? shift.getRevenues() : List.of();

		// 1. Calculate base remuneration for all entries (grouped by config)
		var entriesByConfig = entries.stream()
				.filter(e -> e.getRevenue() != null)
				.collect(groupingBy(e -> {
					DriverRemunerationConfig c = shift.getAppliedConfigForEntry(e.getEntryCategory(), e.getFlatRateType());
					if (c == null) throw new BadRequestException("domain.shift.missing_config_for_" + e.getEntryCategory().name().toLowerCase() + "_entry");
					return c;
				}));

		for (var entry : entriesByConfig.entrySet()) {
			DriverRemunerationConfig config = entry.getKey();

			BigDecimal sumRevenue = entry.getValue().stream()
					.map(ShiftRevenueEntry::getRevenue)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			totalRevenue = totalRevenue.add(sumRevenue);

			IRemunerationCalculator calculator = getCalculatorForType(config.getType());
			RemunerationSplit split = calculator.calculateRemuneration(sumRevenue, config);
			
			totalDriverRemuneration = totalDriverRemuneration.add(split.driverRemuneration()).setScale(2, java.math.RoundingMode.HALF_UP);
			totalCompanyRemuneration = totalCompanyRemuneration.add(split.companyRemuneration()).setScale(2, java.math.RoundingMode.HALF_UP);
		}

		RemunerationSplit finalSplit = new RemunerationSplit(totalCompanyRemuneration, totalDriverRemuneration);

		List<DriverRemunerationConfig> activeConfigs = shift.getAppliedRemunerationConfigs();
		
		// 2. Global check to ensure no unauthorized weekly rent is processed
		boolean hasWeeklyConfig = activeConfigs.stream()
				.anyMatch(c -> c.getType() == RemunerationModelType.WEEKLY_FIXED_RATE);
		if (shift.getWeeklyDriverRent() != null && !hasWeeklyConfig) {
			throw new BadRequestException("domain.shift.weekly_driver_rent_not_allowed");
		}

		// 3. Apply shift-level adjustments based on active configs
		for (DriverRemunerationConfig config : activeConfigs) {
			IRemunerationCalculator calculator = getCalculatorForType(config.getType());
			finalSplit = calculator.applyShiftAdjustments(shift, config, finalSplit);
		}

		ShiftSettlement settlement = shift.getSettlement();
		if (settlement == null) {
			settlement = shiftSettlementMapper.toEntity(shift, totalRevenue, finalSplit);
			shift.setSettlement(settlement);
		} else {
			shiftSettlementMapper.updateEntity(settlement, totalRevenue, finalSplit);
		}

		return settlement;
	}

	private IRemunerationCalculator getCalculatorForType(RemunerationModelType type) {
		return calculators.stream()
				.filter(c -> c.getSupportedModelType() == type)
				.findFirst()
				.orElseThrow(() -> new BadRequestException("domain.remuneration.unsupported_model"));
	}

	public ShiftSettlement calculate(Shift shift, BigDecimal weeklyDriverRentInput) {
		if (weeklyDriverRentInput != null) {
			shift.setWeeklyDriverRent(weeklyDriverRentInput);
		}
		return calculate(shift);
	}
}
