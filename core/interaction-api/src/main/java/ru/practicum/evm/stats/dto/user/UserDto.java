package ru.practicum.evm.stats.dto.user;

import lombok.Builder;

@Builder
public record UserDto(
		Long id,
		String name,
		String email
) {
}
