package ch.springframeworkguru.spring6authserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TokenRequestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testClientCredentialsTokenRequest() throws Exception {
        String content = getAccessTokenResponse();
        assertAll(
            () -> assertTrue(content.contains("access_token"), "Response should contain access_token"),
            () -> assertTrue(content.contains("token_type"), "Response should contain token_type"),
            () -> assertTrue(content.contains("expires_in"), "Response should contain expires_in"),
            () -> assertTrue(content.contains("scope"), "Response should contain scope")
        );
    }

    @Test
    public void testTokenIntrospection() throws Exception {
        String clientId = "messaging-client";
        String clientSecret = "secret";
        String authorization = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        
        String tokenResponse = getAccessTokenResponse();
        String accessToken = extractAccessToken(tokenResponse);

        // Now, test the introspection endpoint
        MvcResult introspectionResult = mockMvc.perform(post("/oauth2/introspect")
                .header("Authorization", "Basic " + authorization)
                .param("token", accessToken)
                .param("token_type_hint", "access_token"))
            .andExpect(status().isOk())
            .andReturn();

        String introspectionContent = introspectionResult.getResponse().getContentAsString();
        assertAll(
            () -> assertTrue(introspectionContent.contains("\"active\":true"), "Token should be active"),
            () -> assertTrue(introspectionContent.contains("\"client_id\":\"" + clientId + "\""), "Client ID should match"),
            () -> assertTrue(introspectionContent.contains("\"scope\":\"message.read\""), "Scope should match"),
            () -> assertTrue(introspectionContent.contains("\"token_type\":\"Bearer\""), "Token type should be Bearer")
        );
    }
    
    private String getAccessTokenResponse() throws Exception {
        String clientId = "messaging-client";
        String clientSecret = "secret";

        String authorization = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        MvcResult tokenResult = mockMvc.perform(post("/oauth2/token")
                .header("Authorization", "Basic " + authorization)
                .param("grant_type", "client_credentials")
                .param("scope", "message.read"))
            .andExpect(status().isOk())
            .andReturn();

        return tokenResult.getResponse().getContentAsString();
    }

    private String extractAccessToken(String tokenResponse) {
        // Simple string manipulation to extract the access token
        // In a real-world scenario, you might want to use a JSON parser
        int start = tokenResponse.indexOf("\"access_token\":\"") + "\"access_token\":\"".length();
        int end = tokenResponse.indexOf("\"", start);
        return tokenResponse.substring(start, end);
    }

    
}
