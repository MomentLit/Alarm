package com.example.alarm.controller;

import com.example.alarm.client.MatchingClient;
import com.example.alarm.client.dto.MatchingResponse;
import com.example.alarm.client.dto.MatchingStatus;
import com.example.alarm.entity.Alarm;
import com.example.alarm.global.exception.MatchingClientException;
import com.example.alarm.repository.AlarmRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlarmRepository alarmRepository;

    @MockitoBean
    private MatchingClient matchingClient;

    @Value("${jwt.secret}")
    private String secret;

    @BeforeEach
    void setUp() {
        alarmRepository.deleteAll();
    }

    @Test
    void createAlarmReturnsOk() throws Exception {
        when(matchingClient.getMatching(2L)).thenReturn(matchingResponse("1"));

        mockMvc.perform(post("/alarm/{matching-id}", 2L)
                        .header("Authorization", "Bearer " + createToken("3"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"ooo님이 매칭 요청을 보냈습니다\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("create alarm"));

        assertThat(alarmRepository.findAll())
                .singleElement()
                .satisfies(alarm -> {
                    assertThat(alarm.getUserId()).isEqualTo("1");
                    assertThat(alarm.getMatchingId()).isEqualTo(2L);
                    assertThat(alarm.getDescription()).isEqualTo("ooo님이 매칭 요청을 보냈습니다");
                });
    }

    @Test
    void createAlarmWithBlankDescriptionReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/alarm/{matching-id}", 2L)
                        .header("Authorization", "Bearer " + createToken("3"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"\"}"))
                .andExpect(status().isBadRequest());

        assertThat(alarmRepository.findAll()).isEmpty();
    }

    @Test
    void createAlarmWithoutTokenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/alarm/{matching-id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"ooo님이 매칭 요청을 보냈습니다\"}"))
                .andExpect(status().isForbidden());

        assertThat(alarmRepository.findAll()).isEmpty();
    }

    @Test
    void createAlarmWhenMatchingClientFailsReturnsBadGateway() throws Exception {
        when(matchingClient.getMatching(2L))
                .thenThrow(new MatchingClientException("매칭 정보를 조회할 수 없습니다."));

        mockMvc.perform(post("/alarm/{matching-id}", 2L)
                        .header("Authorization", "Bearer " + createToken("3"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"ooo님이 매칭 요청을 보냈습니다\"}"))
                .andExpect(status().isBadGateway());

        assertThat(alarmRepository.findAll()).isEmpty();
    }

    @Test
    void getAlarmsReturnsOwnAlarmsLatestFirst() throws Exception {
        alarmRepository.save(new Alarm("1", 2L, "ooo님이 매칭 요청을 보냈습니다"));
        alarmRepository.save(new Alarm("1", 3L, "xxx님이 매칭 요청을 보냈습니다"));
        alarmRepository.save(new Alarm("2", 4L, "다른 사용자의 알람입니다"));

        mockMvc.perform(get("/alarm")
                        .header("Authorization", "Bearer " + createToken("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("get alarms"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].matchingId").value(3))
                .andExpect(jsonPath("$.data[0].description").value("xxx님이 매칭 요청을 보냈습니다"))
                .andExpect(jsonPath("$.data[0].isRead").value(false))
                .andExpect(jsonPath("$.data[1].matchingId").value(2))
                .andExpect(jsonPath("$.data[1].description").value("ooo님이 매칭 요청을 보냈습니다"))
                .andExpect(jsonPath("$.data[1].isRead").value(false));
    }

    @Test
    void getAlarmsReturnsEmptyListWhenNoAlarms() throws Exception {
        mockMvc.perform(get("/alarm")
                        .header("Authorization", "Bearer " + createToken("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("get alarms"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getAlarmsWithoutTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/alarm"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateReadMarksOwnAlarmAsRead() throws Exception {
        Alarm own = alarmRepository.save(new Alarm("1", 2L, "ooo님이 매칭 요청을 보냈습니다"));

        mockMvc.perform(patch("/alarm/{alarm-id}", own.getId())
                        .header("Authorization", "Bearer " + createToken("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("update alarm read"));

        assertThat(alarmRepository.findById(own.getId()).orElseThrow().isRead()).isTrue();
    }

    @Test
    void updateReadDoesNotChangeOtherUsersAlarm() throws Exception {
        Alarm other = alarmRepository.save(new Alarm("2", 2L, "다른 사용자의 알람입니다"));

        mockMvc.perform(patch("/alarm/{alarm-id}", other.getId())
                        .header("Authorization", "Bearer " + createToken("1")))
                .andExpect(status().isOk());

        assertThat(alarmRepository.findById(other.getId()).orElseThrow().isRead()).isFalse();
    }

    @Test
    void updateReadWithoutTokenReturnsForbidden() throws Exception {
        Alarm own = alarmRepository.save(new Alarm("1", 2L, "ooo님이 매칭 요청을 보냈습니다"));

        mockMvc.perform(patch("/alarm/{alarm-id}", own.getId()))
                .andExpect(status().isForbidden());

        assertThat(alarmRepository.findById(own.getId()).orElseThrow().isRead()).isFalse();
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

    private String createToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", "ROLE_USER")
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
