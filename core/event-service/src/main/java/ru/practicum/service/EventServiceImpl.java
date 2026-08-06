package ru.practicum.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.practicum.feignClient.ParticipationClient;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.feignClient.UserClient;
import ru.practicum.dto.*;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.StateMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.enums.AdminStateAction;
import ru.practicum.enums.EventState;
import ru.practicum.enums.ParticipationStatus;
import ru.practicum.enums.UserStateAction;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.EventSpecifications;
import ru.practicum.stat.dto.ViewStatsDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {

	private final StatRepository statRepository;
	private final EventRepository eventRepository;
	private final CategoryRepository categoryRepository;
	private final UserClient userClient;
	private final ParticipationClient participationClient;

	@Override
	public List<EventShortDto> getFreeEvents(@NonNull FreeGetDto dto, HttpServletRequest request) {

		if (dto.rangeStart() != null && dto.rangeEnd() != null) {
			if (dto.rangeEnd().isBefore(dto.rangeStart())) {
				throw new ValidationException("Окончание события не может быть раньше начала");
			}
		}

		statRepository.sendHitRequest(request);

		SpecBuilder<Event> builder = SpecBuilder.<Event>builder()
				.and(EventSpecifications.isPublished())
				.andIf(dto.text() != null && !dto.text().isBlank(),
						() -> EventSpecifications.textContains(dto.text()))
				.andIf(dto.categories() != null && !dto.categories().isEmpty(),
						() -> EventSpecifications.hasCategories(dto.categories()))
				.andIf(dto.paid() != null,
						() -> EventSpecifications.isPaid(dto.paid()))
				.andIf(Boolean.TRUE.equals(dto.onlyAvailable()),
						() -> EventSpecifications.onlyAvailable(true));

		boolean hasStart = dto.rangeStart() != null;
		boolean hasEnd = dto.rangeEnd() != null;

		if (!hasStart && !hasEnd) {
			builder.and(EventSpecifications.eventDateAfterNow(LocalDateTime.now()));
		} else {
			builder
					.andIf(hasStart,
							() -> EventSpecifications.dateAfter(dto.rangeStart()))
					.andIf(hasEnd,
							() -> EventSpecifications.dateBefore(dto.rangeEnd()));
		}

		Specification<Event> spec = builder.build();

		Sort sort = Sort.unsorted();

		if (dto.sort() != null) {
			switch (dto.sort()) {
				case FreeGetDto.FreeEventSort.EVENT_DATE -> sort = Sort.by("eventDate").ascending();
				case FreeGetDto.FreeEventSort.VIEWS -> sort = Sort.by("views").descending();
			}
		}

		Pageable pageable = PageRequest.of(
				dto.from() / dto.size(),
				dto.size(),
				sort
		);

		List<Event> events = eventRepository.findAll(spec, pageable).getContent();
		if (events.isEmpty()) {
			return Collections.emptyList();
		}

		List<EventRequestCountDto> eventRequestCountList = participationClient.countConfirmedRequestsByEventIds(
				events.stream().map(Event::getId).toList(), ParticipationStatus.CONFIRMED);

		Map<Long, Long> requestCountMap = new HashMap<>();
		if (!eventRequestCountList.isEmpty()) {
			eventRequestCountList.forEach(eventRequestCount -> {
				requestCountMap.put(eventRequestCount.getEventId(), eventRequestCount.getCount());
			});
		}

		List<String> uris = events.stream().map(event -> "/events/" + event.getId()).toList();
		List<ViewStatsDto> stats = statRepository.getStat(
				uris,
				dto.rangeStart(),
				dto.rangeEnd(),
				false);

		return events.stream()
				.map(event -> EventMapper.toEventShortDto(
								event,
								requestCountMap.get(event.getId()) == null ? 0L : requestCountMap.get(event.getId()),
								stats.size()
						)
				).toList();
	}

	@Override
	public EventFullDto getFreeEventById(Long eventId, HttpServletRequest request) {
		if (!eventRepository.existsByIdAndState(eventId, EventState.PUBLISHED)) {
			throw new NotFoundException("Событие с id=" + eventId + " не существует или не опубликовано.");
		}
		statRepository.sendHitRequest(request);
		Event event = eventRepository.findById(eventId).orElseThrow(
				() -> new NotFoundException("Событие с id=" + eventId + " не найдено")
		);

		long views = statRepository.getStat(List.of(request.getRequestURI()), true).getFirst().getHits();

		long confirmedRequests = getConfirmedRequests(eventId);

		return EventMapper.toEventFullDto(event, confirmedRequests, views);
	}

	@Override
	public EventFullDto userAddNewEvent(Long userId, @NonNull NewEventDto newEventDto) {
		if (newEventDto.participantLimit() != null && newEventDto.participantLimit() < 0) {
			throw new ValidationException("Ограничение на количество участников должно быть положительным числом");
		}

		if (newEventDto.eventDate() != null && newEventDto.eventDate().isBefore(LocalDateTime.now().plusHours(2))) {
			throw new ValidationException("Начало события не может быть раньше, " +
					"чем через два часа от текущего момента");
		}

		UserDto initiator = getUserById(userId);
		Category category = getCategoryById(newEventDto.category());
		Event event = EventMapper.toEntity(
				newEventDto,
				category,
				LocalDateTime.now(),
				null,
				EventState.PENDING
		);

		event.setInitiatorId(initiator.id());
		Event savedEvent = eventRepository.save(event);
		EventFullDto eventFullDto = EventMapper.toEventFullDto(savedEvent, getConfirmedRequests(event.getId()),
				getHits(event.getId()));
		eventFullDto.setInitiator(initiator);
		return eventFullDto;
	}

	@Override
	public List<EventFullDto> adminGetEvents(@NonNull AdminGetDto dto) {
		final List<Long> validUserIds;

		if (dto.users() != null && !dto.users().isEmpty()) {
			try {
				List<UserDto> users = userClient.getUsersByIds(dto.users());
				validUserIds = users.stream()
						.map(UserDto::id)
						.collect(Collectors.toList());

				if (validUserIds.isEmpty()) {
					return Collections.emptyList();
				}
			} catch (FeignException e) {
				log.error("Ошибка при получении пользователей: {}", e.getMessage());
				throw new ServiceException("Не удалось проверить пользователей");
			}
		} else {
			validUserIds = null;  // Инициализируем в else
		}

		Specification<Event> spec = SpecBuilder.<Event>builder()
				.andIf(dto.states() != null && !dto.states().isEmpty(),
						() -> EventSpecifications.hasStates(dto.states()))
				.andIf(dto.categories() != null && !dto.categories().isEmpty(),
						() -> EventSpecifications.hasCategories(dto.categories()))
				.andIf(dto.rangeStart() != null,
						() -> EventSpecifications.dateAfter(dto.rangeStart()))
				.andIf(dto.rangeEnd() != null,
						() -> EventSpecifications.dateBefore(dto.rangeEnd()))
				.andIf(validUserIds != null && !validUserIds.isEmpty(),
						() -> EventSpecifications.hasUsers(validUserIds))
				.build();

		Pageable pageable = PageRequest.of(
				dto.from() / dto.size(),
				dto.size()
		);

		List<Event> events = eventRepository.findAll(spec, pageable).getContent();
		if (events.isEmpty()) {
			return Collections.emptyList();
		}

		List<EventRequestCountDto> eventRequestCountList = participationClient.countConfirmedRequestsByEventIds(
				events.stream().map(Event::getId).toList(), ParticipationStatus.CONFIRMED);

		Map<Long, Long> requestCountMap = new HashMap<>();
		if (!eventRequestCountList.isEmpty()) {
			eventRequestCountList.forEach(eventRequestCount ->
					requestCountMap.put(eventRequestCount.getEventId(), eventRequestCount.getCount())
			);
		}

		List<String> uris = events.stream().map(event -> "/events/" + event.getId()).toList();
		List<ViewStatsDto> stats = statRepository.getStat(
				uris,
				dto.rangeStart(),
				dto.rangeEnd(),
				false);

		return events.stream()
				.map(event -> EventMapper.toEventFullDto(
								event,
								getConfirmedRequests(requestCountMap, event.getId()),
								getHits(stats, event.getId())
						)
				).toList();
	}

	@Override
	public EventFullDto adminUpdateEvent(Long eventId, @NonNull UpdateEventAdminRequest request) {
		Event oldEvent = eventRepository.findById(eventId).orElseThrow(
				() -> new NotFoundException("Событие с id=" + eventId + " не найдено")
		);
		Event newEvent;

		if (request.eventDate() != null && request.eventDate().isBefore(LocalDateTime.now().plusHours(2))) {
			throw new ValidationException("Дата события должна быть не может быть раньше, " +
					"чем через два часа от текущего момента");
		}

		if (request.stateAction() == null) {
			newEvent = EventMapper.update(
					oldEvent,
					request,
					oldEvent.getState(),
					oldEvent.getPublishedOn(),
					request.category() == null ?
							Optional.empty() :
							Optional.of(getCategoryById(request.category()))
			);
		} else {

			if (request.stateAction().equals(AdminStateAction.PUBLISH_EVENT) &&
					oldEvent.getState().equals(EventState.PUBLISHED)) {
				throw new ConflictException("Событие с id=" + oldEvent.getId() + " уже опубликовано");
			}

			if (request.stateAction().equals(AdminStateAction.PUBLISH_EVENT) &&
					oldEvent.getState().equals(EventState.CANCELED)) {
				throw new ConflictException("Публикация события с id=" + oldEvent.getId() +
						" уже отменена пользователем");
			}

			if (request.stateAction().equals(AdminStateAction.REJECT_EVENT) &&
					oldEvent.getState().equals(EventState.PUBLISHED)) {
				throw new ConflictException("Событие с id=" + oldEvent.getId() +
						" уже опубликовано, отмена не возможна");
			}

			newEvent = EventMapper.update(
					oldEvent,
					request,
					request.stateAction().equals(AdminStateAction.REJECT_EVENT) ?
							EventState.CANCELED : EventState.PUBLISHED,
					request.stateAction().equals(AdminStateAction.REJECT_EVENT) ?
							null : LocalDateTime.now(),
					request.category() == null ?
							Optional.empty() :
							Optional.of(getCategoryById(request.category()))
			);
		}

		EventFullDto eventFullDto = EventMapper.toEventFullDto(
				eventRepository.save(newEvent),
				getConfirmedRequests(oldEvent.getId()),
				getHits(oldEvent.getId())
		);
		UserDto initiator = userClient.getUserById(oldEvent.getInitiatorId());
		eventFullDto.setInitiator(initiator);
		return eventFullDto;
	}

	@Override
	public List<EventShortDto> findByUserId(Long userId, Integer from, Integer size) {
		checkUser(userId);

		PageRequest pageRequest = PageRequest.of(from / size, size);
		Collection<Event> events = eventRepository.findByInitiatorId(userId, pageRequest);
		if (events.isEmpty()) {
			return Collections.emptyList();
		}

		List<EventRequestCountDto> eventRequestCountList = participationClient.countConfirmedRequestsByEventIds(
				events.stream().map(Event::getId).toList(), ParticipationStatus.CONFIRMED);

		Map<Long, Long> requestCountMap = new HashMap<>();
		if (!eventRequestCountList.isEmpty()) {
			eventRequestCountList.forEach(eventRequestCount ->
					requestCountMap.put(eventRequestCount.getEventId(), eventRequestCount.getCount())
			);
		}

		List<String> uris = events.stream().map(event -> "/events/" + event.getId()).toList();
		List<ViewStatsDto> stats = statRepository.getStat(uris, true);

		return events.stream()
				.map(event -> EventMapper.toEventShortDto(
								event,
								getConfirmedRequests(requestCountMap, event.getId()),
								getHits(stats, event.getId())
						)
				).toList();
	}

	@Override
	public EventFullDto findEventById(Long userId, Long eventId) {
		checkUser(userId);

		Event event = eventRepository.findById(eventId).orElseThrow(
				() -> new NotFoundException("Событие с id=" + eventId + " не найдено")
		);

		if (!event.getInitiatorId().equals(userId)) {
			throw new ConflictException("Пользователь должен быть инициатором");
		}

		return assemblyFullDto(event);
	}

	@Override
	public EventFullDto patchEvent(Long userId, Long eventId, @NonNull UpdateEventUserRequest request) {
		if (request.participantLimit() != null && request.participantLimit() < 0) {
			throw new ValidationException("Ограничение на количество участников должно быть положительным числом");
		}

		checkUser(userId);
		return patchEvent(eventId, request, 2, false);
	}

	private EventFullDto patchEvent(Long eventId, @NonNull UpdateEventUserRequest request, long hoursBeforeStart,
	                                boolean isAdmin) {
		try {
			Event event = eventRepository.findById(eventId).orElseThrow(
					() -> new NotFoundException("Событие с id=" + eventId + " не найдено")
			);

			if (!isAdmin && event.getState() == EventState.PUBLISHED) {
				throw new ConflictException("Нельзя редактировать опубликованное событие");
			}

			if (request.eventDate() != null) {
				LocalDateTime eventDateTime = request.eventDate();
				LocalDateTime minDateTime = LocalDateTime.now().plusHours(hoursBeforeStart);

				if (!isAdmin && eventDateTime.isBefore(minDateTime)) {
					throw new ValidationException(
							String.format("Дата события должна быть не ранее чем за %d часа(ов) до начала",
									hoursBeforeStart)
					);
				}
			}

			UserStateAction action = request.stateAction();

			if (action != null) {
				EventState newState = isAdmin
						? StateMapper.mapAdminEventAction(action)
						: StateMapper.mapUserEventAction(action);

				if (EventState.PUBLISHED.equals(newState)) {
					event.setPublishedOn(LocalDateTime.from(Instant.now()));
				}
				if (newState != null) {
					event.setState(newState);
				}
			}

			if (request.category() != null) {
				Category category = getCategoryById(request.category());
				event.setCategory(category);
			}

			EventMapper.merge(event, request);

			Event patched = eventRepository.save(event);

			log.info("Ивент обновлен: {}", patched.getId());

			return assemblyFullDto(event);

		} catch (DataIntegrityViolationException e) {
			log.debug("Конфликт вовремя обновления ивента {}", request, e);
			throw new ConflictException("Конфликт с другим ивентом");
		}
	}

	private void checkUser(Long userId) {
		userClient.checkUser(userId);
	}

	@NonNull
	private UserDto getUserById(long userId) {
		return userClient.getUserById(userId);
	}

	public EventFullDto getEventById(long eventId) {
		Event event = eventRepository.findById(eventId).orElseThrow(
				() -> new NotFoundException("Событие с id=" + eventId + " не найдено")
		);
		return assemblyFullDto(event);
	}

	@NonNull
	private Category getCategoryById(long categoryId) {
		return categoryRepository.findById(categoryId).orElseThrow(
				() -> new NotFoundException("Категория с id=" + categoryId + " не найдена")
		);
	}

	private long getConfirmedRequests(Long eventId) {
		return participationClient.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
	}

	private long getHits(long eventId) {
		List<ViewStatsDto> stats = statRepository.getStat(List.of("/events/" + eventId), true);
		if (stats.isEmpty()) {
			return 0;
		}
		return stats.getFirst().getHits();
	}

	private long getHits(@NonNull List<ViewStatsDto> stats, long eventId) {
		if (stats.isEmpty()) {
			return 0;
		}
		for (ViewStatsDto stat : stats) {
			if (stat.getUri().equals("/events/" + eventId)) {
				return stat.getHits();
			}
		}
		return 0;
	}

	private long getConfirmedRequests(@NonNull Map<Long, Long> requestCountMap, long eventId) {
		if (requestCountMap.isEmpty()) {
			return 0;
		}
		if (requestCountMap.containsKey(eventId)) {
			return requestCountMap.get(eventId);
		}
		return 0;
	}

	public EventFullDto updateEventRate(EventFullDto eventFullDto) {
		Event event = eventRepository.findById(eventFullDto.getId())
				.orElseThrow(() -> new NotFoundException("Событие не найдено"));
		event.setRate(eventFullDto.getRate());
		Event savedEvent = eventRepository.save(event);

		return assemblyFullDto(savedEvent);
	}

	@Override
	public EventFullDto findByIdAndState(Long id, EventState state) {
		Event event = eventRepository.findByIdAndState(id, state);

		if (event == null) {
			throw new NotFoundException("Событие с ID " + id + " не найдено в состоянии " + state);
		}
		return assemblyFullDto(event);
	}

	private EventFullDto assemblyFullDto(Event event) {
		UserDto initiator = userClient.getUserById(event.getInitiatorId());
		EventFullDto eventFullDto = EventMapper.toEventFullDto(event, getConfirmedRequests(event.getId()),
				getHits(event.getId()));
		eventFullDto.setInitiator(initiator);
		return eventFullDto;
	}
}
