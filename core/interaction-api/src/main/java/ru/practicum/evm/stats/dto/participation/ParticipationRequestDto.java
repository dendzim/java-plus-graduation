package ru.practicum.evm.stats.dto.participation;

import lombok.Builder;
import ru.practicum.evm.stats.enums.ParticipationStatus;

@Builder
public record ParticipationRequestDto(
		Long id,
		String created,
		Long event,
		Long requester,
		ParticipationStatus status
) {
}
