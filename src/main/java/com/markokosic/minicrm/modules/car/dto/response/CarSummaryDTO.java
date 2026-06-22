package com.markokosic.minicrm.modules.car.dto.response;

public record CarSummaryDTO(
		Long id,
		String licensePlate,
		String brand,
		String model
) {}