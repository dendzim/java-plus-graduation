package ru.practicum.evm.stats.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.practicum.evm.stats.util.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "categories", schema = "event_service")
@SuperBuilder
@NoArgsConstructor
public class Category extends BaseEntity {

	@Column(nullable = false, length = 50, unique = true)
	private String name;
}
