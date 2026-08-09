package ru.practicum.evm.stats.stat.server.mapper;

import org.springframework.lang.NonNull;
import ru.practicum.evm.stats.stat.dto.EndpointHitDto;
import ru.practicum.evm.stats.stat.server.model.EndpointHit;

public class EndpointHitMapper {

	public static EndpointHit toEntity(@NonNull EndpointHitDto dto) {
		return EndpointHit.builder()
				.app(dto.getApp())
				.uri(dto.getUri())
				.ip(dto.getIp())
				.timestamp(dto.getTimestamp())
				.build();
	}
}
