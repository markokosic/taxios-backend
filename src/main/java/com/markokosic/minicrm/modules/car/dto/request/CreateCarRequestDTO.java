package com.markokosic.minicrm.modules.car.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCarRequestDTO(
    @NotBlank String licensePlate,
    String model,
    String brand,
    String horsepower
) {}
