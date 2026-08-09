package ru.practicum.evm.stats.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.evm.stats.enums.Reaction;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
	private Long id;
	private Long userId;
	private Long eventId;
	private Reaction reaction;
}
