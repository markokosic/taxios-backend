package com.markokosic.minicrm.modules.flatratetype.repository;

import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateTypeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlatRateTypeRepository extends JpaRepository<FlatRateType, Long> {
	List<FlatRateType> findAllByCurrentIsTrueAndStatus(FlatRateTypeStatus status);
	List<FlatRateType> findAllByCurrentIsTrue();
}
