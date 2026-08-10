package ru.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.inteface.ParticipationOperations;

@FeignClient(name = "request-service", path = "/api/request")
public interface ParticipationClient extends ParticipationOperations {
}
