package com.santanna.couponapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santanna.couponapi.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CouponControllerIntegrationTest {

    private static final String TODAY = "2026-04-05";
    private static final String FUTURE_DATE = "2026-04-15";
    private static final String PAST_DATE = "2026-04-04";

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(
                    Instant.parse("2026-04-05T12:00:00Z"),
                    ZoneId.of("America/Sao_Paulo")
            );
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
    }

    @Test
    void shouldCreateCouponAndReturnSanitizedCode() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("code", "AB-12@CD");

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code", is("AB12CD")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.published", is(false)));
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("description", "");

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenExpirationDateIsInThePast() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("expirationDate", PAST_DATE);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", is("Expiration date cannot be in the past")));
    }

    @Test
    void shouldAcceptTodayAsExpirationDate() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("expirationDate", TODAY);

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expirationDate", is(TODAY)));
    }

    @Test
    void shouldSoftDeleteCoupon() throws Exception {
        Map<String, Object> request = validRequest();

        String responseBody = mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(delete("/coupon/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownCoupon() throws Exception {
        mockMvc.perform(delete("/coupon/{id}", "2d4dbdc2-10da-4c20-bf93-85915d894f1a"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Coupon not found")));
    }

    @Test
    void shouldNotDeleteCouponTwice() throws Exception {
        Map<String, Object> request = validRequest();

        String responseBody = mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(delete("/coupon/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/coupon/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Coupon not found")));
    }

    private Map<String, Object> validRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("code", "ABC123");
        request.put("description", "Summer campaign");
        request.put("discountValue", new BigDecimal("10.00"));
        request.put("expirationDate", FUTURE_DATE);
        request.put("published", false);
        return request;
    }
}