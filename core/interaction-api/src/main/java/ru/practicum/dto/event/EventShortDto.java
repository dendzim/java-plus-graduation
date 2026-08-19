package ru.practicum.dto.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.dto.category.CategoryDto;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class EventShortDto {
	private long id;
	private String annotation;
	private CategoryDto category;
	private long confirmedRequests;
	private LocalDateTime eventDate;
	private UserShortDto initiator;
	private boolean paid;
	private String title;
	private Double rating;
	private long rate;
}
