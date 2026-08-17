package ru.practicum.service.compilation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationSearchFilter;
import ru.practicum.dto.compilation.CompilationUpdateDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.exception.NotFoundException;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompilationServiceImpl implements CompilationService {

	private final CompilationRepository compilationRepository;
	private final EventRepository eventRepository;
	private final CompilationMapper compilationMapper;

	@Override
	public CompilationDto getById(Long compilationId) {

		Compilation compilation = compilationRepository.findById(compilationId)
				.orElseThrow(() -> new NotFoundException("Подборка с id " + compilationId + " не найдена"));

		return compilationMapper.toCompilationDto(compilation);
	}

	@Override
	@Transactional
	public void delById(Long compilationId) {
		getCompilationById(compilationId);
		compilationRepository.deleteById(compilationId);
	}

	@Override
	@Transactional
	public CompilationDto addCompilation(@NonNull NewCompilationDto compilationDto) {
		Set<Event> events = new HashSet<>();

		if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
			events = new HashSet<>(eventRepository.findAllById(compilationDto.getEvents()));

			if (events.size() < compilationDto.getEvents().size()) {
				throw new NotFoundException("Одно или несколько событий не найдены");
			}
		}

		Compilation compilation = compilationMapper.toEntity(compilationDto);
		compilation.setEvents(events);
		Compilation savedCompilation = compilationRepository.save(compilation);

		return compilationMapper.toCompilationDto(savedCompilation);
	}

	@Override
	@Transactional
	public CompilationDto updateCompilation(Long compilationId, @NonNull CompilationUpdateDto compilationUpdateDto) {
		Compilation compilationInDb = getCompilationById(compilationId);

		compilationMapper.merge(compilationInDb, compilationUpdateDto);

		if (compilationUpdateDto.getEvents() != null) {
			compilationInDb.setEvents(getEventsFromIds(compilationUpdateDto.getEvents()));
		}

		return compilationMapper.toCompilationDto(compilationInDb);
	}

	@Override
	public List<CompilationDto> getByFilter(@NonNull CompilationSearchFilter filter) {
		Pageable pageable = PageRequest.of(filter.getFrom() / filter.getSize(), filter.getSize());
		Page<Compilation> compilationsPage;

		if (filter.getPinned() != null) {
			compilationsPage = compilationRepository.findAllByPinned(filter.getPinned(), pageable);
		} else {
			compilationsPage = compilationRepository.findAll(pageable);
		}

		List<Compilation> compilations = compilationsPage.getContent();

		if (compilations.isEmpty()) {
			return Collections.emptyList();
		}

		return compilations.stream()
				.map(compilationMapper::toCompilationDto)
				.toList();
	}

	@NonNull
	private Compilation getCompilationById(long compilationId) {
		return compilationRepository.findById(compilationId).orElseThrow(
				() -> new NotFoundException("Подборка с id=" + compilationId + " не найдена")
		);
	}

	private Set<Event> getEventsFromIds(Set<Long> eventIds) {
		if (eventIds == null || eventIds.isEmpty()) {
			return Collections.emptySet();
		}
		return new HashSet<>(eventRepository.findAllById(eventIds));
	}
}
