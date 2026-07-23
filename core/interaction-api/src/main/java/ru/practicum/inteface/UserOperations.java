package ru.practicum.inteface;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;

import java.util.List;

public interface UserOperations {

    /**
     * Добавление нового пользователя
     *
     * @param newUserRequest Данные добавляемого пользователя
     * @return {@link UserDto}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserDto addNewUser(@RequestBody @Valid NewUserRequest newUserRequest);

    /**
     * Получение информации о пользователях
     * <p>
     * Возвращает информацию обо всех пользователях (учитываются параметры ограничения выборки),
     * либо о конкретных (учитываются указанные идентификаторы)
     * В случае, если по заданным фильтрам не найдено ни одного пользователя, возвращает пустой список
     *
     * @param ids  id пользователей
     * @param from количество элементов, которые нужно пропустить для формирования текущего набора
     *             Default value : 0
     * @param size количество элементов в наборе
     *             Default value : 10
     * @return List<{@link UserDto}>
     */
    @GetMapping
    List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    /**
     * Удаление пользователя
     *
     * @param userId id пользователя
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@PathVariable Long userId);
}
