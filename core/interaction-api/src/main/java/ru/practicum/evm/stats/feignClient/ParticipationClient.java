package ru.practicum.evm.stats.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.evm.stats.inteface.ParticipationOperations;

@FeignClient(name = "request-service", path = "/api/request")
public interface ParticipationClient extends ParticipationOperations {
}
