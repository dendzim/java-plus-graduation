package ru.practicum.evm.stats.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.evm.stats.enums.ParticipationStatus;
import ru.practicum.evm.stats.util.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "requests", schema = "request_service")
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	LocalDateTime created;

	@Column(name = "event_id", nullable = false)
	Long eventId;

	@Column(name = "requester_id", nullable = false)
	Long requesterId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	ParticipationStatus status;
}
