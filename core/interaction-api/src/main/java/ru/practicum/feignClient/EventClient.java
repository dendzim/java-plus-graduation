package ru.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.inteface.EventOperations;

@FeignClient(name = "event-service", path = "/api/event")
public interface EventClient extends EventOperations {
}
