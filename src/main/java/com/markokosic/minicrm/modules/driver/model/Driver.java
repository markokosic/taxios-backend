package com.markokosic.minicrm.modules.driver.model;

import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.shift.FlatRateType;
import com.markokosic.minicrm.modules.shift.ShiftEntryCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.TenantId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="drivers")
public class Driver {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@TenantId
	@Column(name = "tenant_id", nullable = false, updatable = false)
	private Long tenantId;

	@Column(name="first_name")
	private String firstName;

	@Column(name="last_name")
	private String lastName;

	@Column(name="email")
	private String email;

	@Column(name="phone")
	private String phone;

	@BatchSize(size = 25)
	@OneToMany(
			mappedBy = "driver",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private List<DriverRemunerationConfig> remunerationConfigs = new ArrayList<>();

	private String getConfigKey(DriverRemunerationConfig config) {
		if (config.getType() == RemunerationModelType.FLAT_RATE) {
			return config.getType() + "_" + (config.getFlatRateType() != null ? config.getFlatRateType().getId() : "ALL");
		}
		return config.getType().name();
	}

	private String getConfigKey(CreateRemunerationRequestDTO request) {
		if (request instanceof com.markokosic.minicrm.modules.driver.dto.request.CreateFlatRateRemunerationConfigDTO flatDto) {
			return request.remunerationModelType() + "_" + (flatDto.flatRateTypeId() != null ? flatDto.flatRateTypeId() : "ALL");
		}
		return request.remunerationModelType().name();
	}

	public void syncRemunerationConfigs(
			List<CreateRemunerationRequestDTO> requests,
			Function<CreateRemunerationRequestDTO, DriverRemunerationConfig> mapper
	) {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);

		// get current active configs by composite key
		Map<String, DriverRemunerationConfig> currentActiveMap = this.remunerationConfigs.stream()
				.filter(DriverRemunerationConfig::isCurrent)
				.collect(Collectors.toMap(this::getConfigKey, c -> c));

		Set<String> keysInRequest = new HashSet<>();

		for (CreateRemunerationRequestDTO request : requests) {
			String key = getConfigKey(request);
			keysInRequest.add(key);

			DriverRemunerationConfig existing = currentActiveMap.get(key);

			if (existing != null) {
				if (!existing.isIdenticalTo(request)) {
					existing.deactivate(yesterday);
					DriverRemunerationConfig newConfig = mapper.apply(request);
					newConfig.activate(today);
					newConfig.setDriver(this);
					this.remunerationConfigs.add(newConfig);
				}
			} else {
				DriverRemunerationConfig newConfig = mapper.apply(request);
				newConfig.activate(today);
				newConfig.setDriver(this);
				this.remunerationConfigs.add(newConfig);
			}
		}

		//deactivate any configs that were not in the request - basically delete a remuneration config.
		currentActiveMap.values().stream()
				.filter(c -> !keysInRequest.contains(getConfigKey(c)))
				.forEach(c -> c.deactivate(yesterday));
	}

	public void initializeWithRemunerationConfigs(List<DriverRemunerationConfig> newConfigs) {
		if (newConfigs == null || newConfigs.isEmpty()) {
			throw new IllegalArgumentException("Initial remuneration configs cannot be empty");
		}

		this.remunerationConfigs.clear();
		for (DriverRemunerationConfig config : newConfigs) {
			config.activate(LocalDate.now());
			config.setDriver(this);
			remunerationConfigs.add(config);
		}
	}

	public DriverRemunerationConfig getCurrentRemunerationConfig() {
		return this.remunerationConfigs.stream()
				.filter(DriverRemunerationConfig::isCurrent)
				.findFirst()
				.orElse(null);
	}

	public List<DriverRemunerationConfig> getActiveRemunerationConfigs() {
		return this.remunerationConfigs.stream()
				.filter(DriverRemunerationConfig::isCurrent)
				.toList();
	}

	public DriverRemunerationConfig getRemunerationConfigForEntry(ShiftEntryCategory category, FlatRateType flatRateType) {
		if (category == ShiftEntryCategory.FLAT_RATE && flatRateType != null) {
			Optional<DriverRemunerationConfig> specificConfig = this.remunerationConfigs.stream()
					.filter(c -> c.isCurrent() && c.getFlatRateType() != null && flatRateType.getId().equals(c.getFlatRateType().getId()))
					.findFirst();
			if (specificConfig.isPresent()) {
				return specificConfig.get();
			}
		}

		RemunerationModelType targetType = switch (category) {
			case REGULAR -> RemunerationModelType.PERCENTAGE_SHARE;
			case FLAT_RATE -> RemunerationModelType.FLAT_RATE;
			case WEEKLY -> RemunerationModelType.WEEKLY_FIXED_RATE;
		};

		return this.remunerationConfigs.stream()
				.filter(c -> c.isCurrent() && c.getType() == targetType && c.getFlatRateType() == null)
				.findFirst()
				.orElseGet(this::getCurrentRemunerationConfig);
	}

	public DriverRemunerationConfig getCurrentRemunerationConfigByType(RemunerationModelType type) {
		return this.remunerationConfigs.stream()
				.filter(c -> c.isCurrent() && c.getType() == type)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Driver has no configuration of type: " + type));
	}

	public void deactivateConfig(Long configId) {
		this.remunerationConfigs.stream()
				.filter(c -> c.getId().equals(configId) && c.isCurrent())
				.findFirst()
				.ifPresentOrElse(
						c -> c.deactivate(LocalDate.now()),
						() -> { throw new IllegalArgumentException("Active configuration with ID " + configId + " not found for this driver."); }
				);
	}

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name="status", nullable = false)
	@Enumerated(EnumType.STRING)
	private DriverStatus status;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		this.status = DriverStatus.ACTIVE;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

}
