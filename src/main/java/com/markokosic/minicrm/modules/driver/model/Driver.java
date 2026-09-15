package com.markokosic.minicrm.modules.driver.model;

import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.user.User;
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

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true)
	private User user;

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
		if (config instanceof FlatRateRemunerationConfig flatConfig) {
			return config.getType() + "_" + (flatConfig.getFlatRateType() != null ? flatConfig.getFlatRateType().getFlatRateCode() : "ALL");
		}
		return config.getType().name();
	}

	public void syncRemunerationConfigs(List<DriverRemunerationConfig> newConfigs) {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);

		// get current active configs by composite key
		Map<String, DriverRemunerationConfig> currentActiveMap = this.remunerationConfigs.stream()
				.filter(DriverRemunerationConfig::isCurrent)
				.collect(Collectors.toMap(this::getConfigKey, c -> c));

		Set<String> keysInRequest = new HashSet<>();

		for (DriverRemunerationConfig newConfig : newConfigs) {
			String key = getConfigKey(newConfig);
			keysInRequest.add(key);

			DriverRemunerationConfig existing = currentActiveMap.get(key);

			if (existing != null) {
				if (!existing.isIdenticalTo(newConfig)) {
					// We NEVER hard delete an existing config because it might be referenced by a shift!
					// Instead, we just deactivate it.
					existing.deactivate(yesterday);
					newConfig.activate(today);
					newConfig.setDriver(this);
					this.remunerationConfigs.add(newConfig);
				}
			} else {
				newConfig.activate(today);
				newConfig.setDriver(this);
				this.remunerationConfigs.add(newConfig);
			}
		}

		currentActiveMap.values().stream()
				.filter(c -> !keysInRequest.contains(getConfigKey(c)))
				.forEach(c -> {
					c.deactivate(yesterday);
				});
	}

	public void initializeWithRemunerationConfigs(List<DriverRemunerationConfig> newConfigs) {
		if (newConfigs == null || newConfigs.isEmpty()) {
			throw new IllegalArgumentException("Initial remuneration configs cannot be empty");
		}
		if (!this.remunerationConfigs.isEmpty()) {
			throw new IllegalStateException("Cannot initialize configs for a driver that already has configs");
		}

		for (DriverRemunerationConfig config : newConfigs) {
			config.activate(LocalDate.now());
			config.setDriver(this);
			remunerationConfigs.add(config);
		}
	}

	//TODO remove
	public DriverRemunerationConfig getCurrentRemunerationConfig() {
		return this.remunerationConfigs.stream()
				.map(org.hibernate.Hibernate::unproxy)
				.map(DriverRemunerationConfig.class::cast)
				.filter(DriverRemunerationConfig::isCurrent)
				.findFirst()
				.orElse(null);
	}

	public List<DriverRemunerationConfig> getActiveRemunerationConfigs() {
		return this.remunerationConfigs.stream()
				.map(org.hibernate.Hibernate::unproxy)
				.map(DriverRemunerationConfig.class::cast)
				.filter(DriverRemunerationConfig::isCurrent)
				.toList();
	}

	public Optional<FlatRateRemunerationConfig> findFlatRateConfig(FlatRateType flatRateType) {
		if (flatRateType == null) {
			return Optional.empty();
		}
		return this.remunerationConfigs.stream()
				.map(org.hibernate.Hibernate::unproxy)
				.map(DriverRemunerationConfig.class::cast)
				.filter(DriverRemunerationConfig::isCurrent)
				.filter(c -> c instanceof FlatRateRemunerationConfig fc && fc.getFlatRateType() != null &&
						((flatRateType.getId() != null && flatRateType.getId().equals(fc.getFlatRateType().getId())) ||
						 (flatRateType.getFlatRateCode() != null && flatRateType.getFlatRateCode().equalsIgnoreCase(fc.getFlatRateType().getFlatRateCode()))))
				.map(c -> (FlatRateRemunerationConfig) c)
				.findFirst();
	}

	public DriverRemunerationConfig getRemunerationConfigForEntry(ShiftEntryCategory category, FlatRateType flatRateType) {
		if (category == ShiftEntryCategory.FLAT_RATE && flatRateType != null) {
			Optional<FlatRateRemunerationConfig> specificConfig = findFlatRateConfig(flatRateType);
			if (specificConfig.isPresent()) {
				return specificConfig.get();
			}
		}

		if (category == ShiftEntryCategory.REGULAR) {
			Optional<DriverRemunerationConfig> percentageConfig = this.remunerationConfigs.stream()
					.map(org.hibernate.Hibernate::unproxy)
					.map(DriverRemunerationConfig.class::cast)
					.filter(c -> c.isCurrent() && c.getType() == RemunerationModelType.PERCENTAGE_SHARE)
					.findFirst();
			if (percentageConfig.isPresent()) {
				return percentageConfig.get();
			}
			Optional<DriverRemunerationConfig> weeklyConfig = this.remunerationConfigs.stream()
					.map(org.hibernate.Hibernate::unproxy)
					.map(DriverRemunerationConfig.class::cast)
					.filter(c -> c.isCurrent() && c.getType() == RemunerationModelType.WEEKLY_FIXED_RATE)
					.findFirst();
			if (weeklyConfig.isPresent()) {
				return weeklyConfig.get();
			}
		}

		RemunerationModelType targetType = switch (category) {
			case REGULAR -> RemunerationModelType.PERCENTAGE_SHARE;
			case FLAT_RATE -> RemunerationModelType.FLAT_RATE;
			case WEEKLY -> RemunerationModelType.WEEKLY_FIXED_RATE;
		};

		return this.remunerationConfigs.stream()
				.map(org.hibernate.Hibernate::unproxy)
				.map(DriverRemunerationConfig.class::cast)
				.filter(c -> {
					if (!c.isCurrent() || c.getType() != targetType) return false;
					if (c instanceof FlatRateRemunerationConfig fc) return fc.getFlatRateType() == null;
					return true;
				})
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No active remuneration config found for entry category: " + category));
	}

	//TODO remove
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
