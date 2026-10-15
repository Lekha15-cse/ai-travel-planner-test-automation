package com.travelplanner.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * UI validation tests for the AI Travel Planner web application.
 * Verifies client-side form calculation logic, date range validations,
 * and hidden payload synchronization without submitting the form or invoking AI services.
 */
public class TravelPlannerValidationTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("Validation: Calculate duration for valid dates and display error for inverted date range")
    void shouldValidateDateRangeAndCalculateDuration() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true);

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // 1. Navigate to home page
                page.navigate(APP_URL);

                Locator startDate = page.locator("#startDate");
                Locator endDate = page.locator("#endDate");
                Locator durationDisplay = page.locator("#durationDisplay");
                Locator hiddenDuration = page.locator("#duration");

                // 2. Set valid date range: 2026-11-01 to 2026-11-05 (5 days inclusive)
                startDate.fill("2026-11-01");
                endDate.fill("2026-11-05");
                endDate.dispatchEvent("change");

                // 3. Assert duration calculation for valid range
                assertEquals("5 days total", durationDisplay.textContent().trim(),
                        "Duration display should show '5 days total' for Nov 1 to Nov 5");
                assertEquals("5 days", hiddenDuration.inputValue().trim(),
                        "Hidden duration input should be populated with '5 days'");

                // 4. Invert date range: set endDate before startDate (2026-10-25 < 2026-11-01)
                endDate.fill("2026-10-25");
                endDate.dispatchEvent("change");

                // 5. Assert validation message and hidden input reset
                assertEquals("End date must be after start date", durationDisplay.textContent().trim(),
                        "Duration display should show error message when end date is before start date");
                assertEquals("", hiddenDuration.inputValue().trim(),
                        "Hidden duration input should be reset to empty string when dates are invalid");
            }
        }
    }
}
