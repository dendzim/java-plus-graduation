package ru.practicum.evm.stats.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.evm.stats.dto.user.UserDto;
import ru.practicum.evm.stats.dto.category.CategoryDto;
import ru.practicum.evm.stats.enums.EventState;
import ru.practicum.evm.stats.util.Location;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EventFullDto {

	private Long id;

	@NotBlank
	private String annotation;

	@NotNull
	private CategoryDto category;

	private Long confirmedRequests;

	private LocalDateTime createdOn;

	private String description;

	@NotNull
	private LocalDateTime eventDate;

	@NotNull
	private UserDto initiator;

	@NotNull
	private Location location;

	private boolean paid;

	private Integer participantLimit;

	private LocalDateTime publishedOn;

	private boolean requestModeration;

	private EventState state;

	@NotBlank
	private String title;

	private Long views;

	private Long rate;
}