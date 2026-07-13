package com.example.alarm.repository;

import com.example.alarm.entity.Alarm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AlarmRepositoryTest {

    @Autowired
    private AlarmRepository alarmRepository;

    @Test
    void saveAndFindAlarm() {
        Alarm alarm = new Alarm("1", 2L, "ooo님이 매칭 요청을 보냈습니다");

        Alarm saved = alarmRepository.save(alarm);

        Alarm found = alarmRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getUserId()).isEqualTo("1");
        assertThat(found.getMatchingId()).isEqualTo(2L);
        assertThat(found.getDescription()).isEqualTo("ooo님이 매칭 요청을 보냈습니다");
        assertThat(found.isRead()).isFalse();
    }

    @Test
    void findAllByUserIdOrderByIdDescReturnsOwnAlarmsLatestFirst() {
        Alarm first = alarmRepository.save(new Alarm("1", 2L, "ooo님이 매칭 요청을 보냈습니다"));
        Alarm second = alarmRepository.save(new Alarm("1", 3L, "xxx님이 매칭 요청을 보냈습니다"));
        alarmRepository.save(new Alarm("2", 4L, "다른 사용자의 알람입니다"));

        List<Alarm> alarms = alarmRepository.findAllByUserIdOrderByIdDesc("1");

        assertThat(alarms)
                .extracting(Alarm::getId)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    void findByIdAndUserIdReturnsOnlyOwnAlarm() {
        Alarm own = alarmRepository.save(new Alarm("1", 2L, "ooo님이 매칭 요청을 보냈습니다"));
        Alarm other = alarmRepository.save(new Alarm("2", 3L, "다른 사용자의 알람입니다"));

        assertThat(alarmRepository.findByIdAndUserId(own.getId(), "1")).isPresent();
        assertThat(alarmRepository.findByIdAndUserId(other.getId(), "1")).isEmpty();
    }
}
