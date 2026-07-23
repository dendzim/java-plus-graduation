package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.feignClient.UserClient;
import ru.practicum.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
public class AdminUserController implements UserClient {

	private final UserService userService;

	@Override
	public UserDto addNewUser(NewUserRequest newUserRequest) {
		return userService.adminAddNewUser(newUserRequest);
	}

	@Override
	public List<UserDto> getUsers(List<Long> ids, int from, int size) {
		return userService.getUsers(ids, from, size);
	}

	@Override
	public void deleteUser(Long userId) {
		userService.deleteUser(userId);
	}
}
