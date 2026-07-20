package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RemunerationService {


	public RemunerationSplit calculateRemunerationSplitFromDailyRevenue(BigDecimal dailyRevenue, DriverRemunerationConfig config) {

		return config.calculateRemuneration(dailyRevenue);

	}

}
