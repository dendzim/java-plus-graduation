package ru.practicum.evm.stats.service;

import jakarta.transaction.Transactional;
import ru.practicum.evm.stats.dto.user.NewUserRequest;
import ru.practicum.evm.stats.dto.user.UserDto;

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
