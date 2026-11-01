package com.example.soccer_app.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerProfileResponse (
    JsonNode errors,
    Integer results,
    List<PlayerEntry> response
) {
    public boolean hasErrors() {
        if (errors == null || errors.isNull()) {
            return false;
        }

        return !(errors.isContainer() && errors.isEmpty());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerEntry(PlayerProfileDto player) {}
}
