package com.example.alarm.service;

import com.example.alarm.client.MatchingClient;
import com.example.alarm.client.dto.MatchingResponse;
import com.example.alarm.client.dto.MatchingStatus;
import com.example.alarm.dto.request.AlarmCreateRequest;
import com.example.alarm.dto.response.AlarmResponse;
import com.example.alarm.entity.Alarm;
import com.example.alarm.global.exception.MatchingClientException;
import com.example.alarm.repository.AlarmRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private MatchingClient matchingClient;

    @InjectMocks
    private AlarmService alarmService;

    @Test
    void createSavesAlarmWithHostId() {
        when(matchingClient.getMatching(2L)).thenReturn(matchingResponse("1"));
        AlarmCreateRequest request = new AlarmCreateRequest("ooo님이 매칭 요청을 보냈습니다");

        alarmService.create(2L, request);

        ArgumentCaptor<Alarm> captor = ArgumentCaptor.forClass(Alarm.class);
        verify(alarmRepository).save(captor.capture());

        Alarm saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("1");
        assertThat(saved.getMatchingId()).isEqualTo(2L);
        assertThat(saved.getDescription()).isEqualTo("ooo님이 매칭 요청을 보냈습니다");
    }

    @Test
    void createThrowsWhenMatchingClientFails() {
        when(matchingClient.getMatching(2L))
                .thenThrow(new MatchingClientException("매칭 정보를 조회할 수 없습니다."));
        AlarmCreateRequest request = new AlarmCreateRequest("ooo님이 매칭 요청을 보냈습니다");

        assertThatThrownBy(() -> alarmService.create(2L, request))
                .isInstanceOf(MatchingClientException.class);

        verifyNoInteractions(alarmRepository);
    }

    @Test
    void getAlarmsReturnsResponsesFromRepository() {
        when(alarmRepository.findAllByUserIdOrderByIdDesc("1"))
                .thenReturn(List.of(new Alarm("1", 2L, "ooo님이 매칭 요청을 보냈습니다")));

        List<AlarmResponse> alarms = alarmService.getAlarms("1");

        assertThat(alarms)
                .singleElement()
                .satisfies(alarm -> {
                    assertThat(alarm.matchingId()).isEqualTo(2L);
                    assertThat(alarm.description()).isEqualTo("ooo님이 매칭 요청을 보냈습니다");
                    assertThat(alarm.isRead()).isFalse();
                });
    }

    @Test
    void updateReadMarksOwnAlarmAsRead() {
        Alarm alarm = new Alarm("1", 2L, "ooo님이 매칭 요청을 보냈습니다");
        when(alarmRepository.findByIdAndUserId(5L, "1")).thenReturn(Optional.of(alarm));

        alarmService.updateRead("1", 5L);

        assertThat(alarm.isRead()).isTrue();
    }

    @Test
    void updateReadDoesNothingWhenAlarmNotOwned() {
        when(alarmRepository.findByIdAndUserId(5L, "1")).thenReturn(Optional.empty());

        alarmService.updateRead("1", 5L);
    }

    @Test
    void getAlarmsReturnsEmptyListWhenNoAlarms() {
        when(alarmRepository.findAllByUserIdOrderByIdDesc("1")).thenReturn(List.of());

        List<AlarmResponse> alarms = alarmService.getAlarms("1");

        assertThat(alarms).isEmpty();
    }

    private MatchingResponse matchingResponse(String hostId) {
        return new MatchingResponse(
                2L,
                1L,
                hostId,
                "3",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                10000,
                MatchingStatus.REQUESTED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
