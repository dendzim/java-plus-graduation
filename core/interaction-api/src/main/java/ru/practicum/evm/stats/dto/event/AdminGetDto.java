package ru.practicum.evm.stats.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.evm.stats.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@NotNull
@Builder
public record AdminGetDto(
		List<Long> users,
		List<EventState> states,
		List<Integer> categories,
		LocalDateTime rangeStart,
		LocalDateTime rangeEnd,
		Integer from,
		Integer size
) {
}