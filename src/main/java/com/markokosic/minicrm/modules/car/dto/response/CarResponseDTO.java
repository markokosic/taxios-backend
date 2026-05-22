package com.markokosic.minicrm.modules.car.dto.response;

import com.markokosic.minicrm.modules.car.model.CarStatus;
import java.time.LocalDateTime;

public record CarResponseDTO(
    Long id,
    String licensePlate,
    String model,
    String brand,
    String horsepower,
    CarStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
