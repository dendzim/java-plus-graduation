package ru.practicum.inteface;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.UserDto;

import java.util.List;

public interface UserOperations {

    @GetMapping("/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/{userId}/check")
    void checkUser(@PathVariable("userId") Long userId);

    @GetMapping
    List<UserDto> getUsersByIds(@RequestParam("ids") List<Long> ids);
}
