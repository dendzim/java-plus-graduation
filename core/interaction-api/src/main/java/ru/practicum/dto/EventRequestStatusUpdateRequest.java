package ru.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.enums.RequestUpdateStatus;

import java.util.List;

@Builder
public record EventRequestStatusUpdateRequest(

		List<Long> requestIds,

		@NotNull
		RequestUpdateStatus status
) {
}
