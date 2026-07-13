package com.example.alarm.service;

import com.example.alarm.client.MatchingClient;
import com.example.alarm.client.dto.MatchingResponse;
import com.example.alarm.dto.request.AlarmCreateRequest;
import com.example.alarm.dto.response.AlarmResponse;
import com.example.alarm.entity.Alarm;
import com.example.alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public List<AlarmResponse> getAlarms(String userId) {
        return alarmRepository.findAllByUserIdOrderByIdDesc(userId).stream()
                .map(AlarmResponse::from)
                .toList();
    }

    @Transactional
    public void updateRead(String userId, Long alarmId) {
        alarmRepository.findByIdAndUserId(alarmId, userId)
                .ifPresent(Alarm::markAsRead);
    }
}
