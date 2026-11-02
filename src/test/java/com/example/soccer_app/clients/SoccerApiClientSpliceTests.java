package com.example.soccer_app.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.example.soccer_app.client.SoccerApiClient;

@RestClientTest(SoccerApiClient.class)
public class SoccerApiClientSpliceTests {
    
    @Autowired
    private SoccerApiClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void returnsPlayerWhenApiRespondsSuccessfully() {

        server.expect(requestTo(
                    "https://soccer.test/players/profiles?player=7"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                    {
                        "errors": [],
                        "results": 1,
                        "response": [
                        {
                            "player": {
                            "id": 7,
                            "name": "Test Player"
                            }
                        }
                        ]
                    }
                    """, MediaType.APPLICATION_JSON));
        
        
        // Act: call the method being tested.
        var result = client.fetchPlayerProfile(7);

        // Assert: check the returned player.
        assertTrue(result.isPresent());

        var player = result.orElseThrow();
        assertEquals(7L, player.id());
        assertEquals("Test Player", player.name());

        // Check that the expected HTTP request happened.
        server.verify();        
    }

    @Test
    void fetchPlayerProfile_whenPlayerIsEmpty_throwsException() {

        server.expect(requestTo(
                    "https://soccer.test/players/profiles?player=7"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                    {
                        "errors": [],
                        "results": 1,
                        "response": [
                            { "player": {} }
                        ]
                    }
                    """, MediaType.APPLICATION_JSON));

        var exception = assertThrows(RestClientException.class,
                () -> client.fetchPlayerProfile(7));

        assertEquals("External API returned an invalid player", exception.getMessage());
        server.verify();
    }

    @Test
    void fetchPlayerProfile_whenPlayerIsFound_returnsMappedPlayerProfile() {
        
        server.expect(requestTo(
                "https://soccer.test/players/profiles?player=276"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess(
                new ClassPathResource("fixtures/player-profile-found.json"),
                MediaType.APPLICATION_JSON));

        var result = client.fetchPlayerProfile(276);
        assertTrue(result.isPresent());
        var player = result.orElseThrow();

        assertEquals(276L, player.id());
        assertEquals("Test Player", player.name());
        assertEquals("Test", player.firstName());
        assertEquals("Player", player.lastName());
        assertEquals(26, player.age());
        assertEquals("2000-04-15", player.birth().date());
        assertEquals("London", player.birth().place());
        assertEquals("England", player.birth().country());
        assertEquals("England", player.nationality());
        assertEquals("180 cm", player.height());
        assertEquals("75 kg", player.weight());
        assertEquals(10, player.jerseyNumber());
        assertEquals("Midfielder", player.position());
        assertEquals("https://soccer.test/players/276.png", player.photoUrl());

        server.verify();
    }

    @Test
    void fetchPlayerProfile_whenApiReturnsNoContent_throwsException() {
        
        server.expect(requestTo(
                "https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withStatus(HttpStatus.NO_CONTENT));

        var exception = assertThrows(
            RestClientException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals(
            "External API returned an empty body",
            exception.getMessage());

        server.verify();
    }

    @Test
    void fetchPlayerProfile_whenExternalApiReportsError_throwsException() {
        
        server.expect(requestTo(
                "https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                {
                    "errors": {
                        "time": "2019-11-26T00:00:00+00:00",
                        "bug": "This is on our side, please report us this bug",
                        "report": "players/profiles"
                    },
                    "results": 0,
                    "response": []
                }
                """, MediaType.APPLICATION_JSON));

        var exception = assertThrows(
            RestClientException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals("External API reported an error", exception.getMessage());
        server.verify();
    }

    @Test
    void fetchPlayerProfile_whenApiReturns499_throwsClientErrorException() {
        String errorBody = """
            {
                "message": "Something went wrong while fetching details. Try again later."
            }
            """;

        server.expect(requestTo("https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withStatus(HttpStatusCode.valueOf(499))
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorBody));

        var exception = assertThrows(
            HttpClientErrorException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals(499, exception.getStatusCode().value());
        assertEquals(errorBody, exception.getResponseBodyAsString());
        server.verify();
    }

    @Test
    void fetchPlayerProfile_whenApiReturns500_throwsServerErrorException() {
        String errorBody = """
            {
                "message": "Something went wrong while fetching details. Try again later."
            }
            """;

        server.expect(requestTo("https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorBody));

        var exception = assertThrows(
            HttpServerErrorException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals(500, exception.getStatusCode().value());
        assertEquals(errorBody, exception.getResponseBodyAsString());
        server.verify();
    }

    @Test 
    void fetchPlayerProfile_whenResultsIsNull_throwsException() {

        server.expect(requestTo(
            "https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                {
                    "errors": [],
                    "results": null,
                    "response": []
                }
                """, MediaType.APPLICATION_JSON));

        var exception = assertThrows(
            RestClientException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals("External API returned an invalid envelope", exception.getMessage());
        server.verify();

    }

    @Test 
    void fetchPlayerProfile_whenResponseIsNull_throwsException() {

        server.expect(requestTo(
            "https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                {
                    "errors": [],
                    "results": 1,
                    "response": null
                }
                """, MediaType.APPLICATION_JSON));

        var exception = assertThrows(
            RestClientException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals("External API returned an invalid envelope", exception.getMessage());
        server.verify();

    }

    @Test
    void fetchPlayerProfile_whenNoPlayerFound_returnsEmptyOptional() {

        server.expect(requestTo(
            "https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                {
                    "errors": [],
                    "results": 0,
                    "response": []
                }
                """, MediaType.APPLICATION_JSON));

        var result = client.fetchPlayerProfile(100);

        assertTrue(result.isEmpty());
        server.verify();
    }

    @Test
    void fetchPlayerProfile_whenUnexpectedResponseSize_throwsExeception() {

        server.expect(requestTo(
            "https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                {
                    "errors": [],
                    "results": 1,
                    "response": []
                }
                """, MediaType.APPLICATION_JSON));

        var exception = assertThrows(
            RestClientException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals("External API returned unexpected results", exception.getMessage());
        server.verify();

    }

    @Test 
    void fetchPlayerProfile_whenUnexpectedResultCount_throwsException() {
            
        server.expect(requestTo(
            "https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                {
                    "errors": [],
                    "results": 2,
                    "response": [
                        {
                            "player": {
                                "id": 100
                            }
                        }
                    ]
                }
                """, MediaType.APPLICATION_JSON));

        var exception = assertThrows(
            RestClientException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals("External API returned unexpected results", exception.getMessage());
        server.verify();
    }

    @Test
    void fetchPlayerProfile_whenNullResponseBody_throwsExeception() {
            
        server.expect(requestTo(
            "https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                {
                    "errors": [],
                    "results": 1,
                    "response": [null]
                }
                """, MediaType.APPLICATION_JSON));

        var exception = assertThrows(
            RestClientException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals("External API returned an invalid player", exception.getMessage());
        server.verify();
    }

    @Test 
    void fetchPlayerProfile_whenMissingPlayerObject_throwsExeception() {
               
        server.expect(requestTo(
            "https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                {
                    "errors": [],
                    "results": 1,
                    "response": [{}]
                }
                """, MediaType.APPLICATION_JSON));

        var exception = assertThrows(
            RestClientException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals("External API returned an invalid player", exception.getMessage());
        server.verify();
    }

    @Test 
    void fetchPlayerProfile_whenWrongPlayerId_throwsException() {

               server.expect(requestTo(
            "https://soccer.test/players/profiles?player=100"))
            .andExpect(header("x-apisports-key", "test-key"))
            .andRespond(withSuccess("""
                {
                    "errors": [],
                    "results": 1,
                    "response": [
                        {
                            "player": {
                                "id": 101
                            }
                        }
                    ]
                }
                """, MediaType.APPLICATION_JSON));

        var exception = assertThrows(
            RestClientException.class,
            () -> client.fetchPlayerProfile(100));

        assertEquals("External API returned an invalid player", exception.getMessage());
        server.verify();
    }

}
