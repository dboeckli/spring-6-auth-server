package ch.springframeworkguru.spring6authserver.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TokenRequestIT {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CLIENT_ID = "messaging-client";
    private static final String CLIENT_SECRET = "secret";
    private static final String AUTHORIZATION = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());
    private static final String GRANT_TYPE = "client_credentials";

    @Test
    void testClientCredentialsTokenRequest() {
        String tokenUrl = "http://localhost:" + port + "/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " + AUTHORIZATION);

        String body = "grant_type=" + GRANT_TYPE + "&scope=message.read";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            entity,
            String.class
        );

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertTrue(response.getHeaders().getContentType().toString().startsWith("application/json")),
            () -> assertNotNull(response.getBody()),
            () -> assertTrue(response.getBody().contains("access_token")),
            () -> assertTrue(response.getBody().contains("token_type")),
            () -> assertTrue(response.getBody().contains("expires_in")),
            () -> assertTrue(response.getBody().contains("scope")),
            () -> assertTrue(response.getBody().contains("Bearer")),
            () -> assertTrue(response.getBody().contains("message.read"))
        );
    }
}
