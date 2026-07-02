package com.example.alarm.global.util;

import com.example.alarm.global.dto.ApiResponse;

public class ResponseUtil {

    private ResponseUtil() {
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }
}
