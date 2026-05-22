package com.markokosic.minicrm.modules.car;

import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.common.error.ApiErrorCode;
import com.markokosic.minicrm.exception.NotFoundException;
import com.markokosic.minicrm.modules.car.dto.request.CreateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.request.UpdateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.response.CarResponseDTO;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.car.model.CarStatus;
import com.markokosic.minicrm.modules.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final TenantService tenantService;

    @Transactional
    public CarResponseDTO createCar(CreateCarRequestDTO request) {
        Long tenantId = tenantService.getTenantIdFromContextHolder();
        Car car = carMapper.toEntity(request, tenantId);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Transactional(readOnly = true)
    public CarResponseDTO getCarById(Long id) {
        Car car = getCarOrThrow(id);
        return carMapper.toDto(car);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<CarResponseDTO> getAllCars(Pageable pageable) {
        Long tenantId = tenantService.getTenantIdFromContextHolder();
        Page<CarResponseDTO> page = carRepository.findAllByTenantIdAndStatus(tenantId, CarStatus.ACTIVE, pageable)
                .map(carMapper::toDto);
        return PageResponseDTO.from(page);
    }

    @Transactional
    public CarResponseDTO updateCar(Long id, UpdateCarRequestDTO request) {
        Car car = getCarOrThrow(id);
        carMapper.updateEntityFromDto(request, car);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Transactional
    public void deleteCar(Long id) {
        Car car = getCarOrThrow(id);
        if (CarStatus.DELETED.equals(car.getStatus())) {
            throw new NotFoundException(ApiErrorCode.CAR_NOT_FOUND);
        }
        car.setStatus(CarStatus.DELETED);
        carRepository.save(car);
    }

    private Car getCarOrThrow(Long id) {
        Long tenantId = tenantService.getTenantIdFromContextHolder();
        return carRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(ApiErrorCode.CAR_NOT_FOUND));
    }
}
