package com.ipaam.ai.transfer.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class OpenApiConfig {
    @Value("${keycloak.token-uri}")
    private String tokenUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Secured API").version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("keycloak"))
                .components(new Components()
                        .addSecuritySchemes("keycloak",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .in(SecurityScheme.In.HEADER)
                                        .flows(new OAuthFlows()
                                                .password(new OAuthFlow()
                                                        .tokenUrl(tokenUrl
                                                       // .scopes(new Scopes()
                                                               // .addString("email", "OpenID Connect scope")
                                                               // .addString("profile", "Profile information")
                                                        )
                                                )
                                        )
                        )
                );
    }
}
