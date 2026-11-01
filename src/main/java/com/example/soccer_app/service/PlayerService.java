package com.example.soccer_app.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.soccer_app.client.SoccerApiClient;
import com.example.soccer_app.dto.PlayerProfileDto;

@Service 
public class PlayerService {

    private final SoccerApiClient soccerApiClient;

    public PlayerService(SoccerApiClient soccerApiClient) {
        this.soccerApiClient = soccerApiClient;
    } 

    public Optional<PlayerProfileDto> getPlayerProfile(long id) {
        return soccerApiClient.fetchPlayerProfile(id);
    }
    
}
