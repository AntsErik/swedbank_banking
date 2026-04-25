package com.swedbank.bankingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Application entry point for the banking API service.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@SpringBootApplication
public class BankingApiApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(BankingApiApplication.class, args);
    }

    /**
     * Creates a RestClient bean for HTTP communication.
     * Used by exchange rate and external logging clients.
     *
     * @return configured RestClient instance
     */
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
