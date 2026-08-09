package ru.practicum.evm.stats.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.evm.stats.dto.user.UserDto;
import ru.practicum.evm.stats.inteface.UserOperations;
import ru.practicum.evm.stats.service.UserService;

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
