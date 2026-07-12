package com.example.alarm.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record MatchingResponse(
        @JsonProperty("matching_id")
        Long matchingId,

        @JsonProperty("space_id")
        Long spaceId,

        @JsonProperty("host_id")
        String hostId,

        @JsonProperty("seller_id")
        String sellerId,

        @JsonProperty("start_time")
        LocalDateTime startTime,

        @JsonProperty("end_time")
        LocalDateTime endTime,

        @JsonProperty("total_price")
        Integer totalPrice,

        MatchingStatus status,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {
}
