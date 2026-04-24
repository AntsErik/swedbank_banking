package com.swedbank.bankingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
}
