package com.markokosic.minicrm.modules.car.service;

import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.car.CarMapper;
import com.markokosic.minicrm.modules.car.CarRepository;
import com.markokosic.minicrm.modules.car.CarService;
import com.markokosic.minicrm.modules.car.dto.request.CreateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.request.UpdateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.response.CarResponseDTO;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.car.model.CarStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarService carService;

    @Test
    void testCreateCar_whenValidRequest_shouldSaveAndReturnCar() {
        // Arrange
        CreateCarRequestDTO request = new CreateCarRequestDTO("WI-XX-1234", "Golf", "VW", "150");
        Car car = new Car();
        car.setLicensePlate(request.licensePlate());
        car.setBrand(request.brand());
        car.setModel(request.model());
        car.setHorsepower(request.horsepower());

        CarResponseDTO expectedResponse = new CarResponseDTO(
                1L, request.licensePlate(), request.model(), request.brand(), request.horsepower(),
                CarStatus.ACTIVE, null, null
        );

        when(carMapper.toEntity(request)).thenReturn(car);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expectedResponse);

        // Act
        CarResponseDTO result = carService.createCar(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("WI-XX-1234", result.licensePlate());
        verify(carMapper, times(1)).toEntity(request);
        verify(carRepository, times(1)).save(car);
        verify(carMapper, times(1)).toDto(car);
    }

    @Test
    void testGetCarById_whenCarExists_shouldReturnCar() {
        // Arrange
        Long carId = 1L;
        Car car = new Car();
        car.setId(carId);
        car.setLicensePlate("WI-XX-1234");

        CarResponseDTO expectedResponse = new CarResponseDTO(
                carId, "WI-XX-1234", "Golf", "VW", "150",
                CarStatus.ACTIVE, null, null
        );

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expectedResponse);

        // Act
        CarResponseDTO result = carService.getCarById(carId);

        // Assert
        assertNotNull(result);
        assertEquals(carId, result.id());
        assertEquals("WI-XX-1234", result.licensePlate());
        verify(carRepository, times(1)).findById(carId);
    }

    @Test
    void testGetCarById_whenCarDoesNotExist_shouldThrowNotFoundException() {
        // Arrange
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carService.getCarById(carId);
        });

        assertEquals("domain.car.not_found", exception.getMessage());
        verify(carRepository, times(1)).findById(carId);
        verifyNoInteractions(carMapper);
    }

    @Test
    void testGetAllCars_shouldReturnPagedCars() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Car car1 = new Car();
        car1.setId(1L);
        Car car2 = new Car();
        car2.setId(2L);

        List<Car> cars = List.of(car1, car2);
        Page<Car> carPage = new PageImpl<>(cars, pageable, cars.size());

        CarResponseDTO dto1 = new CarResponseDTO(1L, "WI-1", "Model", "Brand", "100", CarStatus.ACTIVE, null, null);
        CarResponseDTO dto2 = new CarResponseDTO(2L, "WI-2", "Model", "Brand", "100", CarStatus.ACTIVE, null, null);

        when(carRepository.findAllByStatus(CarStatus.ACTIVE, pageable)).thenReturn(carPage);
        when(carMapper.toDto(car1)).thenReturn(dto1);
        when(carMapper.toDto(car2)).thenReturn(dto2);

        // Act
        PageResponseDTO<CarResponseDTO> result = carService.getAllCars(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).id());
        assertEquals(2L, result.getContent().get(1).id());
        verify(carRepository, times(1)).findAllByStatus(CarStatus.ACTIVE, pageable);
        verify(carMapper, times(1)).toDto(car1);
        verify(carMapper, times(1)).toDto(car2);
    }


    @Test
    void testUpdateCar_whenCarExists_shouldUpdateAndReturnCar() {
        // Arrange
        Long carId = 1L;
        UpdateCarRequestDTO updateRequest = new UpdateCarRequestDTO("NEW-123", "Golf 8", "VW", "180");
        Car existingCar = new Car();
        existingCar.setId(carId);
        existingCar.setLicensePlate("WI-XX-1234");

        CarResponseDTO expectedResponse = new CarResponseDTO(
                carId, "NEW-123", "Golf 8", "VW", "180",
                CarStatus.ACTIVE, null, null
        );

        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));
        doNothing().when(carMapper).updateEntityFromDto(updateRequest, existingCar);
        when(carRepository.save(existingCar)).thenReturn(existingCar);
        when(carMapper.toDto(existingCar)).thenReturn(expectedResponse);

        // Act
        CarResponseDTO result = carService.updateCar(carId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("NEW-123", result.licensePlate());
        assertEquals("Golf 8", result.model());
        verify(carRepository, times(1)).findById(carId);
        verify(carMapper, times(1)).updateEntityFromDto(updateRequest, existingCar);
        verify(carRepository, times(1)).save(existingCar);
        verify(carMapper, times(1)).toDto(existingCar);
    }

    @Test
    void testUpdateCar_whenCarDoesNotExist_shouldThrowNotFoundException() {
        // Arrange
        Long carId = 1L;
        UpdateCarRequestDTO updateRequest = new UpdateCarRequestDTO("NEW-123", "Golf 8", "VW", "180");
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carService.updateCar(carId, updateRequest);
        });

        assertEquals("domain.car.not_found", exception.getMessage());
        verify(carRepository, times(1)).findById(carId);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    void testDeleteCar_whenCarIsActive_shouldSetStatusToDeletedAndSave() {
        // Arrange
        Long carId = 1L;
        Car car = new Car();
        car.setId(carId);
        car.setStatus(CarStatus.ACTIVE);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carRepository.save(car)).thenReturn(car);

        // Act
        carService.deleteCar(carId);

        // Assert
        assertEquals(CarStatus.DELETED, car.getStatus());
        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, times(1)).save(car);
    }

    @Test
    void testDeleteCar_whenCarDoesNotExist_shouldThrowNotFoundException() {
        // Arrange
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carService.deleteCar(carId);
        });

        assertEquals("domain.car.not_found", exception.getMessage());
        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, times(0)).save(any(Car.class));
    }

    @Test
    void testDeleteCar_whenCarIsAlreadyDeleted_shouldThrowNotFoundException() {
        // Arrange
        Long carId = 1L;
        Car car = new Car();
        car.setId(carId);
        car.setStatus(CarStatus.DELETED);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carService.deleteCar(carId);
        });

        assertEquals("domain.car.not_found", exception.getMessage());
        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, times(0)).save(any(Car.class));
    }
}
