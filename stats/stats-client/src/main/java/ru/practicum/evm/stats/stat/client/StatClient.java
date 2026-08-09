package ru.practicum.evm.stats.stat.client;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.evm.stats.stat.dto.EndpointHitDto;
import ru.practicum.evm.stats.stat.dto.StatsRequest;
import ru.practicum.evm.stats.stat.dto.ViewStatsDto;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
public class StatClient {

	private final String statsServiceId;
	private final RestTemplate rest;
	private final DiscoveryClient discoveryClient;
	private final RetryTemplate retryTemplate;

	public StatClient(DiscoveryClient discoveryClient, RestTemplateBuilder builder,
					  @Value("${stats-service.id:STATS-SERVER}") String statsServiceId) {
		this.statsServiceId = statsServiceId;
		this.discoveryClient = discoveryClient;
		this.rest = builder.build();
		this.retryTemplate = createRetryTemplate();
    }

	public void hit(EndpointHitDto endpointHitDto) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(endpointHitDto, headers);

			rest.exchange(
					makeUriString("/hit"),
					HttpMethod.POST,
					requestEntity,
					Void.class
			);
		} catch (Exception e) {
			log.error("Ошибка записи: {}", endpointHitDto, e);
		}

	}

	public List<ViewStatsDto> getStat(StatsRequest statsRequest) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder
					.fromHttpUrl(makeUriString("/stats"))
					.queryParam("start", statsRequest.getStart())
					.queryParam("end", statsRequest.getEnd())
					.queryParam("unique", statsRequest.getUnique());

			List<String> uris = statsRequest.getUris();

			if (uris != null && !uris.isEmpty()) {
				builder.queryParam("uris", uris);
			}

			URI uri = builder.encode().build().toUri();

			return rest.exchange(
					uri,
					HttpMethod.GET,
					null,
					new ParameterizedTypeReference<List<ViewStatsDto>>() {
					}
			).getBody();

		} catch (Exception e) {
			log.error("Ошибка записи: {}", statsRequest, e);
			return null;
		}
	}

	private ServiceInstance getInstance() {
		try {
			return discoveryClient
					.getInstances(statsServiceId)
					.getFirst();
		} catch (Exception exception) {
			throw new RuntimeException("Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId, exception);
		}
	}

	private String makeUriString(String path) {
		ServiceInstance instance = retryTemplate.execute(context -> getInstance());
		return "http://" + instance.getHost() + ":" + instance.getPort() + path;
	}

	private RetryTemplate createRetryTemplate() {
		RetryTemplate template = new RetryTemplate();

		FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
		fixedBackOffPolicy.setBackOffPeriod(3000L);
		template.setBackOffPolicy(fixedBackOffPolicy);

		MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
		retryPolicy.setMaxAttempts(3);
		template.setRetryPolicy(retryPolicy);
		return template;
	}
}
