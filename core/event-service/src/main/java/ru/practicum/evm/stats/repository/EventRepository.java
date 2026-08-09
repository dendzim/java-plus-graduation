package ru.practicum.evm.stats.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.evm.stats.model.Event;
import ru.practicum.evm.stats.enums.EventState;

import java.util.Collection;

public interface EventRepository extends JpaRepository<Event, Long>,
		JpaSpecificationExecutor<Event> {

	Collection<Event> findByInitiatorId(Long userId, PageRequest pageRequest);

	Event findByIdAndState(Long eventId, EventState state);

	boolean existsByCategoryId(Long categoryId);

	boolean existsByIdAndState(Long eventId, EventState state);

}
