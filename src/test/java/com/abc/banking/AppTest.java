package com.abc.banking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    void healthStatusIsUp() {
        assertEquals("UP", App.healthStatus());
    }
}
