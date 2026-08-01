package ru.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "request-service", path = "/api/request")
public interface ParticipationClient {
}
