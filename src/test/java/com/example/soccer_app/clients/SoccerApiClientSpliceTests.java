package com.example.soccer_app.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;

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
}
