package com.example.soccer_app.client;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.soccer_app.dto.PlayerProfileDto;
import com.example.soccer_app.dto.PlayerProfileResponse;

@Component 
public class SoccerApiClient {
    
    private final RestClient restClient;
    
    // Constructor
    public SoccerApiClient(
        RestClient.Builder builder,
        @Value("${external.soccer-api.base-url}") String baseUrl,
        @Value("${external.soccer-api.api-key}") String apiKey
    ) {
        this.restClient = builder
            .baseUrl(baseUrl)
            .defaultHeader("x-apisports-key", apiKey)
            .build();
    }

    // Get player profile from football-api
    public Optional<PlayerProfileDto> fetchPlayerProfile(long id) {
       PlayerProfileResponse envelope =  restClient.get()
       .uri(uriBuilder -> uriBuilder
        .path("/players/profiles")
        .queryParam("player", id)
        .build())
        .retrieve()
        .body(PlayerProfileResponse.class);
    
        
        if (envelope == null) {
            throw new RestClientException("External API returned an empty body");
        }

        if (envelope.hasErrors()) {
            throw new RestClientException("External API reported an error");
        }

        if (envelope.response() == null || envelope.results() == null){
            throw new RestClientException("External API returned an invalid envelope");
        }

        if (envelope.response().isEmpty() && envelope.results() == 0) {
            return Optional.empty();
        }

        // Only one player should be returned
        if (envelope.response().size() != 1 || envelope.results() != 1) {
            throw new RestClientException("External API returned unexpected results");
        }

        var entry = envelope.response().getFirst();
        PlayerProfileDto player = entry == null ? null : entry.player();

        if (player == null || player.id() == null || player.id() != id) {
            throw new RestClientException("External API returned an invalid player");
        }

        return Optional.of(player);
    }
}
