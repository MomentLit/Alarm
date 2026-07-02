package com.example.alarm.global.exception;

public class MatchingClientException extends RuntimeException {

    public MatchingClientException(String message) {
        super(message);
    }

    public MatchingClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
