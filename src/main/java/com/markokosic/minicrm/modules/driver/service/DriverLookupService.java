package com.markokosic.minicrm.modules.driver.service;

import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

//TODO refactor this service into driverService
@Component
@RequiredArgsConstructor
public class DriverLookupService {
	private final DriverRepository driverRepository;

	public Driver validateDriverExistsOrThrow(Long id) {
		return driverRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("domain.driver.not_found"));
	}

	public List<Driver> validateAllExistOrThrow(Set<Long> ids){
		List<Driver> drivers = driverRepository.findAllByIdIn(ids);
		if (drivers.size() != ids.size()) {
			throw new ResourceNotFoundException("domain.driver.not_found");
		}
		return drivers;
	}
}
