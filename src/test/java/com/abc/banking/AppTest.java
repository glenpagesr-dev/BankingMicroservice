package com.abc.banking;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppTest {

    @Test
    void healthStatusIsUp() {
        assertEquals("UP", App.healthStatus());
    }

    @Test
    void depositAddsToBalance() {
        assertEquals(new BigDecimal("999.00"),
                App.deposit(new BigDecimal("100.00"), new BigDecimal("50.00")));
    }

    @Test
    void depositRejectsNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> App.deposit(new BigDecimal("100.00"), BigDecimal.ZERO));
    }

    @Test
    void withdrawSubtractsFromBalance() {
        assertEquals(new BigDecimal("75.00"),
                App.withdraw(new BigDecimal("100.00"), new BigDecimal("25.00")));
    }

    @Test
    void withdrawRejectsInsufficientFunds() {
        assertThrows(IllegalArgumentException.class,
                () -> App.withdraw(new BigDecimal("100.00"), new BigDecimal("500.00")));
    }
}