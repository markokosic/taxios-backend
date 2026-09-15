package com.markokosic.minicrm.modules.car;

import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.car.model.CarStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    Page<Car> findAllByStatus(CarStatus status, Pageable pageable);
    List<Car> findAllByStatusOrderByLicensePlateAsc(CarStatus status);
}
