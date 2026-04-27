package com.swedbank.bankingapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for the Banking API.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the OpenAPI metadata for Swagger UI documentation.
     *
     * @return OpenAPI bean with API information
     */
    @Bean
    public OpenAPI bankingApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking API")
                        .description("Multi-currency account management REST API with Swedbank exchange rates")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ants-Erik Noormagi")
                                .email("ants-erik.noormagi@example.com")));
    }
}
