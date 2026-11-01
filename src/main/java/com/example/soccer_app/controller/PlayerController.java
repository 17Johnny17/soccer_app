package com.example.soccer_app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.example.soccer_app.dto.PlayerProfileDto;
import com.example.soccer_app.service.PlayerService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController 
@RequestMapping("/api/players")
public class PlayerController {
    
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/{id}")
    public PlayerProfileDto getPlayer(@PathVariable("id") long id) {
        if (id < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be positive");
        }

        try {
            return playerService.getPlayerProfile(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Player not found"
            ));
        }
        catch (RestClientResponseException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "External API request failed",
                ex
            );
        }
        catch (RestClientException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Could not retrieve player data",
                ex
            );
        }
    }
    
    
}
