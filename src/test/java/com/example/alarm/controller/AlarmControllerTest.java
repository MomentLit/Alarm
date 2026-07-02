package com.example.alarm.controller;

import com.example.alarm.client.MatchingClient;
import com.example.alarm.client.dto.MatchingResponse;
import com.example.alarm.client.dto.MatchingStatus;
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
