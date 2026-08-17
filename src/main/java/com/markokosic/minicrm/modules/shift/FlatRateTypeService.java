package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlatRateTypeService {

	private final FlatRateTypeRepository flatRateTypeRepository;
	private final FlatRateTypeMapper flatRateTypeMapper;

	public List<FlatRateTypeResponseDTO> getAllActiveFlatRateTypes() {
		return flatRateTypeRepository.findAllByCurrentIsTrueAndStatus(FlatRateTypeStatus.ACTIVE).stream()
				.map(flatRateTypeMapper::toDto)
				.toList();
	}

	public List<FlatRateTypeResponseDTO> getAllFlatRateTypes() {
		return flatRateTypeRepository.findAllByCurrentIsTrue().stream()
				.map(flatRateTypeMapper::toDto)
				.toList();
	}

	public FlatRateTypeResponseDTO getFlatRateTypeById(Long id) {
		FlatRateType entity = flatRateTypeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("domain.flat_rate_type.not_found"));
		return flatRateTypeMapper.toDto(entity);
	}

	@Transactional
	public FlatRateTypeResponseDTO createFlatRateType(CreateFlatRateTypeRequestDTO dto) {
		FlatRateType entity = flatRateTypeMapper.toEntity(dto);
		entity.setFlatRateCode(UUID.randomUUID().toString());
		entity.setStatus(FlatRateTypeStatus.ACTIVE);
		entity.activate(LocalDate.now());
		FlatRateType saved = flatRateTypeRepository.save(entity);
		return flatRateTypeMapper.toDto(saved);
	}

	@Transactional
	public FlatRateTypeResponseDTO updateFlatRateType(Long id, CreateFlatRateTypeRequestDTO dto) {
		FlatRateType oldEntity = flatRateTypeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("domain.flat_rate_type.not_found"));
		
		oldEntity.deactivate(LocalDate.now());
		flatRateTypeRepository.save(oldEntity);

		FlatRateType newEntity = flatRateTypeMapper.toEntity(dto);
		newEntity.setFlatRateCode(oldEntity.getFlatRateCode());
		newEntity.setStatus(oldEntity.getStatus());
		newEntity.activate(LocalDate.now());
		
		FlatRateType saved = flatRateTypeRepository.save(newEntity);
		return flatRateTypeMapper.toDto(saved);
	}

	@Transactional
	public void deactivateFlatRateType(Long id) {
		FlatRateType entity = flatRateTypeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("domain.flat_rate_type.not_found"));
		entity.setStatus(FlatRateTypeStatus.DISABLED);
		flatRateTypeRepository.save(entity);
	}
}
