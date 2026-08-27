package com.markokosic.minicrm.modules.car;

import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.car.dto.request.CreateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.request.UpdateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.response.CarResponseDTO;
import com.markokosic.minicrm.modules.car.dto.response.CarSummaryDTO;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.car.model.CarStatus;
import com.markokosic.minicrm.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Transactional
    public CarResponseDTO createCar(CreateCarRequestDTO request) {
        Car car = carMapper.toEntity(request);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Transactional(readOnly = true)
    public CarResponseDTO getCarById(Long id) {
        Car car = getCarOrThrow(id);
        return carMapper.toDto(car);
    }

    @Transactional(readOnly = true)
    public List<CarSummaryDTO> getCarsForSelect() {
        List<Car> cars = carRepository.findAllByStatusOrderByLicensePlateAsc(CarStatus.ACTIVE);
        return carMapper.toSummaryDtoList(cars);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<CarResponseDTO> getAllCars(Pageable pageable) {
        Page<CarResponseDTO> page = carRepository.findAllByStatus(CarStatus.ACTIVE, pageable)
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
            throw new ResourceNotFoundException("domain.car.not_found");
        }
        car.setStatus(CarStatus.DELETED);
        carRepository.save(car);
    }

    private Car getCarOrThrow(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("domain.car.not_found"));
    }
}
