package com.markokosic.minicrm.modules.car.dto.request;

public record UpdateCarRequestDTO(
    String licensePlate,
    String model,
    String brand,
    String horsepower
) {}
