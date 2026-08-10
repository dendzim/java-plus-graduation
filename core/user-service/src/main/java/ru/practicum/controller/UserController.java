package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.user.UserDto;
import ru.practicum.inteface.UserOperations;
import ru.practicum.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/api/user")
@RequiredArgsConstructor
public class UserController implements UserOperations {

    private final UserService userService;

    @Override
    public UserDto getUserById(Long userId) {
        return userService.findUserById(userId);
    }

    @Override
    public void checkUser(Long userId) {
        userService.checkUser(userId);
    }

    @Override
    public List<UserDto> getUsersByIds(List<Long> ids) {
        return userService.getUsersByIds(ids);
    }
}
