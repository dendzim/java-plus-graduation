package ru.practicum.evm.stats.dto.participation;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.evm.stats.enums.RequestUpdateStatus;

import java.util.List;

@Builder
public record EventRequestStatusUpdateRequest(

		List<Long> requestIds,

		@NotNull
		RequestUpdateStatus status
) {
}
