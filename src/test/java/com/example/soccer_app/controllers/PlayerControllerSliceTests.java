package com.example.soccer_app.controllers;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpServerErrorException;

import com.example.soccer_app.controller.PlayerController;
import com.example.soccer_app.dto.PlayerProfileDto;
import com.example.soccer_app.dto.PlayerProfileResponse;
import com.example.soccer_app.service.PlayerService;

import tools.jackson.databind.ObjectMapper;


@WebMvcTest(PlayerController.class)
public class PlayerControllerSliceTests {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private PlayerService playerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test 
    @WithMockUser 
    void getPlayer_whenIdIsNegative_returnsBadRequest() throws Exception {
        
        mockMvc.perform(get("/api/players/-1")).andExpect(status().isBadRequest());

        verifyNoInteractions(playerService);
    }

    @Test
    @WithMockUser
    void getPlayer_whenIdIsZero_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/players/0"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(playerService);
    }

    @Test 
    @WithMockUser
    void getPlayer_whenPlayerIsNotFound_returnsNotFound() throws Exception {
        when(playerService.getPlayerProfile(17L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/players/17"))
            .andExpect(status().isNotFound());

        verify(playerService).getPlayerProfile(17L);
    }
    
    @Test 
    @WithMockUser 
    void getPlayer_whenPlayerIsFound_returnsPlayer() throws Exception {

        var resource = new ClassPathResource(
        "fixtures/player-profile-found.json");

        PlayerProfileDto player;
        try (var input = resource.getInputStream()) {
            var envelope = objectMapper.readValue(
                input, PlayerProfileResponse.class);

            player = envelope.response().getFirst().player();
        }

        when(playerService.getPlayerProfile(276L))
            .thenReturn(Optional.of(player));
    }

    @Test
    @WithMockUser
    void getPlayer_whenClientExceptionOccurs_returnsBadGateway() throws Exception {
        when(playerService.getPlayerProfile(17L))
            .thenThrow(new RestClientException("Could not retrieve player data"));

        mockMvc.perform(get("/api/players/17")).andExpect(status().isBadGateway());

        verify(playerService).getPlayerProfile(17L);
    }

    @Test 
    @WithMockUser 
    void getPlayer_whenClientResponseExceptionOccurs_returnsBadGateway() throws Exception {

        when(playerService.getPlayerProfile(17L))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/api/players/17")).andExpect(status().isBadGateway());

        verify(playerService).getPlayerProfile(17L);
    }
}
