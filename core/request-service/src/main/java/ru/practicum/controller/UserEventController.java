package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.participation.EventRequestStatusUpdateRequest;
import ru.practicum.dto.participation.EventRequestStatusUpdateResult;
import ru.practicum.dto.participation.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class UserEventController {

    private final RequestService service;

    /**
     * Получение информации о запросах на участие в событии текущего пользователя
     * <p>
     * В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список
     *
     * @param userId  id текущего пользователя
     * @param eventId id события
     * @return List<{@link ParticipationRequestDto}>
     */
    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId,
                                                     @PathVariable @Positive Long eventId) {
        return service.findByEventId(userId, eventId);
    }

    /**
     * Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
     * <p>
     * Обратите внимание:
     * <p>
     * - если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
     * <p>
     * - нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)
     * <p>
     * - статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
     * <p>
     * - если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки
     * необходимо отклонить
     *
     * @param userId  id текущего пользователя
     * @param eventId id события текущего пользователя
     * @param status  Новый статус для заявок на участие в событии текущего пользователя
     * @return {@link EventRequestStatusUpdateResult}
     */
    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult patchRequests(@PathVariable @Positive Long userId,
                                                        @PathVariable @Positive Long eventId,
                                                        @RequestBody @Valid EventRequestStatusUpdateRequest status) {
        return service.updateStatusRequest(userId, eventId, status);
    }
}
