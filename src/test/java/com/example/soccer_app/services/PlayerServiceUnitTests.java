package com.example.soccer_app.services;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import com.example.soccer_app.client.SoccerApiClient;
import com.example.soccer_app.dto.PlayerProfileDto;
import com.example.soccer_app.service.PlayerService;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceUnitTests {
    
    @Mock 
    private SoccerApiClient soccerApiClient;

    @InjectMocks 
    private PlayerService playerService;
        
    @Test 
    void getPlayerProfile_whenPlayerIsFound_returnsPlayerProfile() {
        var player = new PlayerProfileDto(
            276L, "Test Player", "Test", "Player", 26,
            new PlayerProfileDto.Birth("2000-04-15", "London", "England"),
            "England", "180 cm", "75 kg", 10, "Midfielder",
            "https://soccer.test/players/276.png");
        when(soccerApiClient.fetchPlayerProfile(276L)).thenReturn(Optional.of(player));

        var result = playerService.getPlayerProfile(276L);

        assertSame(player, result.orElseThrow());
        verify(soccerApiClient).fetchPlayerProfile(276L);
    }

    @Test
    void getPlayerProfile_whenNoPlayerIsFound_returnsEmptyOptional() {
        when(soccerApiClient.fetchPlayerProfile(100L)).thenReturn(Optional.empty());

        var result = playerService.getPlayerProfile(100L);

        assertTrue(result.isEmpty());
        verify(soccerApiClient).fetchPlayerProfile(100L);
    }

    @Test
    void getPlayerProfile_whenClientThrows_propagatesException() {
        var failure = new RestClientException("External API reported an error");
        when(soccerApiClient.fetchPlayerProfile(100L)).thenThrow(failure);

        var exception = assertThrows(
            RestClientException.class,
            () -> playerService.getPlayerProfile(100L));

        assertSame(failure, exception);
        verify(soccerApiClient).fetchPlayerProfile(100L);
    }
}
