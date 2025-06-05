package ch.springframeworkguru.spring6authserver.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "Spring Auth Server",
        description = "Some long and useful description",
        version = "TODO",
        license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
    ),
    security = {
        @SecurityRequirement(name = "bearerAuth"),
        @SecurityRequirement(name = "basicAuth")
    }
)

@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {

    private final BuildProperties buildProperties;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("basicAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic"))
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
        return openApi -> {
            io.swagger.v3.oas.models.info.Info info = openApi.getInfo();
            info.setTitle(buildProperties.getName());
            info.setVersion(buildProperties.getVersion());
        };
    }
}
