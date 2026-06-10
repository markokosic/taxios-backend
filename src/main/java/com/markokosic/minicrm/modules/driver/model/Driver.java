package com.markokosic.minicrm.modules.driver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="drivers")
public class Driver {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id", nullable = false)
	@NotNull
	private Long tenantId;

	@Column(name="first_name")
	private String firstName;

	@Column(name="last_name")
	private String lastName;

	@Column(name="email")
	private String email;

	@Column(name="phone")
	private String phone;

	@OneToMany(
			mappedBy = "driver",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private List<DriverRemunerationConfig> remunerationConfigs = new ArrayList<>();

	public void replaceRemunerationConfigs(List<DriverRemunerationConfig> newConfigs) {
		if (newConfigs == null || newConfigs.isEmpty()) {
			throw new IllegalArgumentException("remuneration configs cannot be empty");
		}

		LocalDate today = LocalDate.now();

		for (DriverRemunerationConfig newConfig : newConfigs) {
			// 1. Deactivate current configs of the same type
			this.remunerationConfigs.stream()
					.filter(c -> c.isCurrent() && c.getType() == newConfig.getType())
					.forEach(c -> c.deactivate(today.minusDays(1)));

			// 2. Activate and add new config
			newConfig.activate(today);
			newConfig.setDriver(this);
			this.remunerationConfigs.add(newConfig);
		}
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

	public java.util.Optional<DriverRemunerationConfig> getCurrentRemunerationConfigByType(com.markokosic.minicrm.modules.remuneration.RemunerationModelType type) {
		return this.remunerationConfigs.stream()
				.filter(c -> c.isCurrent() && c.getType() == type)
				.findFirst();
	}

	public DriverRemunerationConfig getActiveFlatRateRemunerationConfig() {
		return getCurrentRemunerationConfigByType(com.markokosic.minicrm.modules.remuneration.RemunerationModelType.FLAT_RATE)
				.orElse(null);
	}

	public DriverRemunerationConfig getActivePrimaryRemunerationConfig() {
		return this.remunerationConfigs.stream()
				.filter(c -> c.isCurrent() && c.getType() != com.markokosic.minicrm.modules.remuneration.RemunerationModelType.FLAT_RATE)
				.findFirst()
				.orElse(getCurrentRemunerationConfig());
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
