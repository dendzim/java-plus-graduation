package ru.practicum.evm.stats.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.evm.stats.dto.participation.ParticipationRequestDto;
import ru.practicum.evm.stats.model.ParticipationRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Component
public class RequestMapper {

	public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
		if (participationRequest == null) return null;
		return ParticipationRequestDto.builder()
				.event(participationRequest.getEventId())
				.requester(participationRequest.getRequesterId())
				.status(participationRequest.getStatus())
				.created(formatDateTime(participationRequest.getCreated()))
				.id(participationRequest.getId())
				.build();
	}

	private static String formatDateTime(LocalDateTime dateTime) {
		if (dateTime == null) return null;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		return dateTime.format(formatter);
	}
}
