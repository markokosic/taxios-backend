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


	public void activateNewRemuneration(DriverRemunerationConfig newConfig) {
		LocalDate today = LocalDate.now();

		this.remunerationConfigs.stream()
			.filter(DriverRemunerationConfig::isCurrent)
				.forEach(config -> {
					config.deactivate(today);
				});

		newConfig.activate(today);
		newConfig.setDriver(this);

		this.remunerationConfigs.add(newConfig);
	}

	public void initializeWithRemuneration(DriverRemunerationConfig config) {
		LocalDate today = LocalDate.now();
		this.remunerationConfigs.stream()
//				.filter(DriverRemunerationConfig::current)
				.forEach(c -> c.deactivate(today.minusDays(1)));

		config.activate(today);
		config.setDriver(this);
		this.remunerationConfigs.add(config);
	}

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name="status", nullable = false)
	@Enumerated(EnumType.STRING)
	private DriverStatus status;

	public DriverRemunerationConfig getCurrentRemunerationConfig() {
		return this.remunerationConfigs.stream()
				.filter(DriverRemunerationConfig::isCurrent)
				.findFirst()
				.orElse(null);
	}

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
