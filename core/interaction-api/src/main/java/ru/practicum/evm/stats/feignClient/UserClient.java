package ru.practicum.evm.stats.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.evm.stats.inteface.UserOperations;

@FeignClient(name = "user-service", path = "/api/user")
public interface UserClient extends UserOperations {
}
