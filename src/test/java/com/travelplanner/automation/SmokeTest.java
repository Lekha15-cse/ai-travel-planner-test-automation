package com.travelplanner.automation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic smoke test to verify that the JUnit 5 test execution framework
 * is correctly configured and working with Java 21 and Maven.
 */
public class SmokeTest {

    @Test
    @DisplayName("Smoke Test: Verify JUnit 5 execution and assertions")
    void shouldExecuteSmokeTestSuccessfully() {
        // Simple assertion to confirm the test engine runs and asserts truth
        boolean frameworkInitialized = true;
        assertTrue(frameworkInitialized, "Test framework should initialize and pass a basic boolean assertion");

        // Verify basic equality assertion
        int expected = 42;
        int actual = 20 + 22;
        assertEquals(expected, actual, "Basic arithmetic assertion should hold true");
    }
}
