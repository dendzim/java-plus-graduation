package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.practicum.enums.Reaction;
import ru.practicum.util.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "ratings", schema = "rating-service")
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rating extends BaseEntity {

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "event_id", nullable = false)
	private Long eventId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Reaction reaction;
}
