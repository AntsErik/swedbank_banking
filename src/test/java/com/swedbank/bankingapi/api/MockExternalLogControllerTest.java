package com.swedbank.bankingapi.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MockExternalLogController}.
 *
 * @author Ants-Erik Noormagi (AEN)
 * @since v1.0
 */
class MockExternalLogControllerTest {

    @Test
    void externalLogReturnsOkResponse() {
        MockExternalLogController controller = new MockExternalLogController();

        assertThat(controller.externalLog().getStatusCode().is2xxSuccessful()).isTrue();
    }
}