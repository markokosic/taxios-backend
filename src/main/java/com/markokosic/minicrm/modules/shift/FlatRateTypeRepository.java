package com.markokosic.minicrm.modules.shift;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlatRateTypeRepository extends JpaRepository<FlatRateType, Long> {
	List<FlatRateType> findAllByCurrentIsTrueAndStatus(FlatRateTypeStatus status);
	List<FlatRateType> findAllByCurrentIsTrue();
}
