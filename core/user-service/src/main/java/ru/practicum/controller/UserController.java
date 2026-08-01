package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.UserDto;
import ru.practicum.inteface.UserOperations;
import ru.practicum.service.UserService;

@RestController
@RequestMapping(path = "/api/users")
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

}
