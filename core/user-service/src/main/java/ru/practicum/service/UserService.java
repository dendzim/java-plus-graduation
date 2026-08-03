package ru.practicum.service;

import jakarta.transaction.Transactional;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;

import java.util.List;

public interface UserService {

	@Transactional
	UserDto adminAddNewUser(NewUserRequest newUserRequest);

	List<UserDto> getUsers(List<Long> ids, int from, int size);

	@Transactional
	void deleteUser(Long userId);

	UserDto findUserById(Long userId);

	void checkUser(Long userId);

    List<UserDto> getUsersByIds(List<Long> ids);
}
