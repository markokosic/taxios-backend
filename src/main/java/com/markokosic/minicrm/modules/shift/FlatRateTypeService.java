package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlatRateTypeService {

	private final FlatRateTypeRepository flatRateTypeRepository;
	private final FlatRateTypeMapper flatRateTypeMapper;

	public List<FlatRateTypeResponseDTO> getAllActiveFlatRateTypes() {
		return flatRateTypeRepository.findAllByActiveTrue().stream()
				.map(flatRateTypeMapper::toDto)
				.toList();
	}

	public List<FlatRateTypeResponseDTO> getAllFlatRateTypes() {
		return flatRateTypeRepository.findAll().stream()
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
		FlatRateType saved = flatRateTypeRepository.save(entity);
		return flatRateTypeMapper.toDto(saved);
	}

	@Transactional
	public FlatRateTypeResponseDTO updateFlatRateType(Long id, CreateFlatRateTypeRequestDTO dto) {
		FlatRateType entity = flatRateTypeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("domain.flat_rate_type.not_found"));
		flatRateTypeMapper.updateEntityFromDto(dto, entity);
		FlatRateType saved = flatRateTypeRepository.save(entity);
		return flatRateTypeMapper.toDto(saved);
	}

	@Transactional
	public void deactivateFlatRateType(Long id) {
		FlatRateType entity = flatRateTypeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("domain.flat_rate_type.not_found"));
		entity.setActive(false);
		flatRateTypeRepository.save(entity);
	}
}
