package ru.practicum.evm.stats.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.evm.stats.inteface.EventOperations;

@FeignClient(name = "event-service", path = "/api/event")
public interface EventClient extends EventOperations {
}
