package ru.practicum.evm.stats.dto.event;

import lombok.Builder;
import ru.practicum.evm.stats.dto.user.UserShortDto;
import ru.practicum.evm.stats.dto.category.CategoryDto;

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
