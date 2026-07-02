package com.example.alarm.client;

import com.example.alarm.client.dto.MatchingResponse;
import com.example.alarm.global.exception.MatchingClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MatchingClient {

    private final RestClient restClient;

    public MatchingClient(
            @Value("${matching-service.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public MatchingResponse getMatching(Long matchingId) {
        MatchingResponse response = restClient.get()
                .uri("/internal/matchings/{matchingId}", matchingId)
                .retrieve()
                .body(MatchingResponse.class);

        if (response == null) {
            throw new MatchingClientException("매칭 정보를 조회할 수 없습니다.");
        }

        return response;
    }
}
