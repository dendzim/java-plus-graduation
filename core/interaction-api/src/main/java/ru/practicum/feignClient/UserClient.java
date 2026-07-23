package ru.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.inteface.UserOperations;

@FeignClient(name = "user-service", path = "/api/v1/user")
public interface UserClient extends UserOperations {
}
