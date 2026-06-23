package com.transactionriskengine.aiexplanation.common.api;

import com.transactionriskengine.aiexplanation.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void returnsNotFoundResponseForMissingExplanation() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        var response = handler.handleResourceNotFound(
                new ResourceNotFoundException("AI explanation not found")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("AI explanation not found");
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
