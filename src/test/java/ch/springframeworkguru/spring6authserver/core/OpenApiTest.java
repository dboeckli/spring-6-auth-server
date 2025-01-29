package ch.springframeworkguru.spring6authserver.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
class OpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BuildProperties buildProperties;

    @Test
    void openapiGetJsonTest() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        log.info("Response:\n{}", objectMapper.writeValueAsString(jsonNode));

        assertThat(jsonNode.has("info")).isTrue();
        JsonNode infoNode = jsonNode.get("info");
        assertThat(infoNode.has("title")).isTrue();
        assertThat(infoNode.get("title").asText()).isEqualTo(buildProperties.getName());
    }

}
