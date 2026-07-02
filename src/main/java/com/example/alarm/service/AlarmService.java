package com.example.alarm.service;

import com.example.alarm.client.MatchingClient;
import com.example.alarm.client.dto.MatchingResponse;
import com.example.alarm.dto.request.AlarmCreateRequest;
import com.example.alarm.entity.Alarm;
import com.example.alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final MatchingClient matchingClient;

    public void create(Long matchingId, AlarmCreateRequest request) {
        MatchingResponse matching = matchingClient.getMatching(matchingId);

        Alarm alarm = new Alarm(matching.hostId(), matchingId, request.description());

        alarmRepository.save(alarm);
    }
}
