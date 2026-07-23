package ru.practicum.ewm.dto.request;

import lombok.Builder;
import ru.practicum.enums.ParticipationStatus;

@Builder
public record ParticipationRequestDto(
		Long id,
		String created,
		Long event,
		Long requester,
		ParticipationStatus status
) {
}
