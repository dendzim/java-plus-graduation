package ru.practicum.inteface;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.UserDto;

public interface UserOperations {

    @GetMapping("/{userId}")
    UserDto getUserById(@PathVariable Long userId);

    @GetMapping("/{userId}/check")
    void checkUser(Long userId);
}
