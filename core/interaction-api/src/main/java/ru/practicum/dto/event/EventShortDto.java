package ru.practicum.dto.event;

import lombok.Builder;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.dto.category.CategoryDto;

import java.time.LocalDateTime;

@Builder
public record EventShortDto(
		long id,
		String annotation,
		CategoryDto category,
		long confirmedRequests,
		LocalDateTime eventDate,
		UserShortDto initiator,
		boolean paid,
		String title,
		long views,
		long rate
) {
}
