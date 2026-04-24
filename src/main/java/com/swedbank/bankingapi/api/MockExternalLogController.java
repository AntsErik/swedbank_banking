package com.swedbank.bankingapi.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Local mock endpoint used to simulate a successful external logging call.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
@RestController
public class MockExternalLogController {

    /**
     * Returns a successful empty response for local debit logging simulation.
     *
     * @return empty HTTP 200 response
     */
    @GetMapping("/mock/external-log")
    public ResponseEntity<Void> externalLog() {
        return ResponseEntity.ok().build();
    }
}
