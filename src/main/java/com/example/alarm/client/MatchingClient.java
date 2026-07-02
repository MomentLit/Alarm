package com.example.alarm.client;

import com.example.alarm.client.dto.MatchingResponse;
import com.example.alarm.global.exception.MatchingClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class MatchingClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final RestClient restClient;

    public MatchingClient(
            @Value("${matching-service.base-url}") String baseUrl
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public MatchingResponse getMatching(Long matchingId) {
        MatchingResponse response;
        try {
            response = restClient.get()
                    .uri("/internal/matchings/{matchingId}", matchingId)
                    .retrieve()
                    .body(MatchingResponse.class);
        } catch (RestClientException e) {
            throw new MatchingClientException("매칭 정보를 조회할 수 없습니다.", e);
        }

        if (response == null) {
            throw new MatchingClientException("매칭 정보를 조회할 수 없습니다.");
        }

        return response;
    }
}
