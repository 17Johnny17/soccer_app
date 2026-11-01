package com.example.soccer_app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerProfileDto (
    Long id,
    String name,
    @JsonAlias("firstname") String firstName,
    @JsonAlias("lastname") String lastName,
    Integer age,
    Birth birth,
    String nationality,
    String height,
    String weight,
    @JsonAlias("number") Integer jerseyNumber,
    String position,
    @JsonAlias("photo") String photoUrl
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Birth(
        String date, 
        String place,
        String country
    ) {}
}
