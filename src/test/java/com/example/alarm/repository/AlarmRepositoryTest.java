package com.example.alarm.repository;

import com.example.alarm.entity.Alarm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

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
    }
}
