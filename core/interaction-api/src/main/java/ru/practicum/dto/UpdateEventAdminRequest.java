package ru.practicum.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import ru.practicum.enums.AdminStateAction;
import ru.practicum.util.Location;

import java.time.LocalDateTime;

@Builder
public record UpdateEventAdminRequest(

		@Size(min = 20, max = 2000)
		String annotation,

		Long category,

		@Size(min = 20, max = 7000)
		String description,

		LocalDateTime eventDate,

		Location location,

		Boolean paid,

		Integer participantLimit,

		Boolean requestModeration,

		AdminStateAction stateAction,

		@Size(min = 3, max = 120)
		String title
) {
}
