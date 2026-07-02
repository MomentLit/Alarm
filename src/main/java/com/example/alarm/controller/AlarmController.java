package com.example.alarm.controller;

import com.example.alarm.dto.request.AlarmCreateRequest;
import com.example.alarm.global.dto.ApiResponse;
import com.example.alarm.global.util.ResponseUtil;
import com.example.alarm.service.AlarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping("/{matching-id}")
    public ResponseEntity<ApiResponse<Void>> create(
            @PathVariable("matching-id") Long matchingId,
            @Valid @RequestBody AlarmCreateRequest request
    ) {
        alarmService.create(matchingId, request);
        ApiResponse<Void> apiResponse = ResponseUtil.success("create alarm", null);

        return ResponseEntity.ok(apiResponse);
    }
}
