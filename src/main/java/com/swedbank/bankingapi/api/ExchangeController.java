package com.swedbank.bankingapi.api;

import com.swedbank.bankingapi.api.dto.ConversionRequest;
import com.swedbank.bankingapi.api.dto.ConversionResponse;
import com.swedbank.bankingapi.service.ExchangeRateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * REST controller for currency exchange operations.
 * Provides conversion between supported currencies using Swedbank exchange rates.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@RestController
@RequestMapping("/api/v1/exchange")
public class ExchangeController {

    /**
     * Service for currency conversion and exchange rate management.
     */
    private final ExchangeRateService exchangeRateService;

    /**
     * Creates the exchange controller with its service dependency.
     *
     * @param exchangeRateService service for exchange rate operations
     */
    public ExchangeController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    /**
     * Converts an amount between two currencies using current Swedbank exchange rates.
     * Supports conversions between EUR, USD, SEK, and GBP.
     *
     * @param request conversion request with amount, source, and target currencies
     * @return conversion response with converted amount and applied exchange rate
     */
    @PostMapping
    public ConversionResponse convert(@Valid @RequestBody ConversionRequest request) {
        BigDecimal converted = exchangeRateService.convert(
                request.amount(),
                request.fromCurrency(),
                request.toCurrency()
        );

        // Calculate the exchange rate used (converted amount / original amount)
        BigDecimal rate = request.amount().signum() == 0
                ? BigDecimal.ONE
                : converted.divide(request.amount(), 4, java.math.RoundingMode.HALF_EVEN);

        return new ConversionResponse(
                request.amount(),
                request.fromCurrency(),
                converted,
                request.toCurrency(),
                rate
        );
    }
}
