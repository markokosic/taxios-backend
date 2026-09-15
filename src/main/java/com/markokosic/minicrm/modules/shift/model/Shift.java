package com.markokosic.minicrm.modules.shift.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "shifts")
public class Shift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@TenantId
	@Column(name = "tenant_id", nullable = false, updatable = false)
	private Long tenantId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "driver_id", nullable = false)
	private Driver driver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "car_id", nullable = false)
	private Car car;

	@Column(name = "odometer_start", nullable = false, precision = 10, scale = 2)
	private BigDecimal odometerStart;

	@Column(name = "odometer_end", nullable = false, precision = 10, scale = 2)
	private BigDecimal odometerEnd;

	@Column(name = "shift_start", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime shiftStart;

	@Column(name = "shift_end", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime shiftEnd;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private ShiftStatus status = ShiftStatus.APPROVED;

	@Column(name = "weekly_driver_rent", precision = 19, scale = 2)
	private BigDecimal weeklyDriverRent;

	@org.hibernate.annotations.BatchSize(size = 25)
	@OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ShiftRevenueEntry> revenues = new ArrayList<>();

	@OneToOne(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private ShiftSettlement settlement;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "shift_applied_remuneration_configs",
			joinColumns = @JoinColumn(name = "shift_id"),
			inverseJoinColumns = @JoinColumn(name = "config_id")
	)
	private List<com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig> appliedRemunerationConfigs = new ArrayList<>();

	public com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig getAppliedConfigForEntry(ShiftEntryCategory category, com.markokosic.minicrm.modules.flatratetype.model.FlatRateType flatRateType) {
		if (category == ShiftEntryCategory.FLAT_RATE && flatRateType != null) {
			java.util.Optional<com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig> specificConfig = this.appliedRemunerationConfigs.stream()
					.map(org.hibernate.Hibernate::unproxy)
					.map(com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig.class::cast)
					.filter(c -> c instanceof com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig fc && fc.getFlatRateType() != null &&
							((flatRateType.getId() != null && flatRateType.getId().equals(fc.getFlatRateType().getId())) ||
							 (flatRateType.getFlatRateCode() != null && flatRateType.getFlatRateCode().equalsIgnoreCase(fc.getFlatRateType().getFlatRateCode()))))
					.map(c -> (com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig) c)
					.findFirst();
			if (specificConfig.isPresent()) {
				return specificConfig.get();
			}
		}

		if (category == ShiftEntryCategory.REGULAR) {
			java.util.Optional<com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig> percentageConfig = this.appliedRemunerationConfigs.stream()
					.map(org.hibernate.Hibernate::unproxy)
					.map(com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig.class::cast)
					.filter(c -> c.getType() == com.markokosic.minicrm.modules.remuneration.RemunerationModelType.PERCENTAGE_SHARE)
					.findFirst();
			if (percentageConfig.isPresent()) {
				return percentageConfig.get();
			}
			java.util.Optional<com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig> weeklyConfig = this.appliedRemunerationConfigs.stream()
					.map(org.hibernate.Hibernate::unproxy)
					.map(com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig.class::cast)
					.filter(c -> c.getType() == com.markokosic.minicrm.modules.remuneration.RemunerationModelType.WEEKLY_FIXED_RATE)
					.findFirst();
			if (weeklyConfig.isPresent()) {
				return weeklyConfig.get();
			}
		}

		com.markokosic.minicrm.modules.remuneration.RemunerationModelType targetType = switch (category) {
			case REGULAR -> com.markokosic.minicrm.modules.remuneration.RemunerationModelType.PERCENTAGE_SHARE;
			case FLAT_RATE -> com.markokosic.minicrm.modules.remuneration.RemunerationModelType.FLAT_RATE;
			case WEEKLY -> com.markokosic.minicrm.modules.remuneration.RemunerationModelType.WEEKLY_FIXED_RATE;
		};

		return this.appliedRemunerationConfigs.stream()
				.map(org.hibernate.Hibernate::unproxy)
				.map(com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig.class::cast)
				.filter(c -> {
					if (c.getType() != targetType) return false;
					if (c instanceof com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig fc) return fc.getFlatRateType() == null;
					return true;
				})
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No applied remuneration config found for entry category: " + category));
	}

	public void addRevenueEntry(ShiftRevenueEntry entry) {
		revenues.add(entry);
		entry.setShift(this);
	}

	public void removeRevenueEntry(ShiftRevenueEntry entry) {
		revenues.remove(entry);
		entry.setShift(null);
	}

	public Duration getShiftDuration() {
		if (shiftStart == null || shiftEnd == null) return Duration.ZERO;
		return Duration.between(shiftStart, shiftEnd);
	}

	public BigDecimal getKilometersDriven() {
		if (odometerStart == null || odometerEnd == null) return BigDecimal.ZERO;
		return odometerEnd.subtract(odometerStart);
	}
}
