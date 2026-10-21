package com.travelplanner.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 3C, Task 6 — Traveler Counter UI Automation Test.
 *
 * Verifies that the traveler counter on the home page:
 *   - Displays an initial count of 2.
 *   - Correctly increments when the '+' button is clicked.
 *   - Correctly decrements when the '-' button is clicked.
 *   - Enforces a minimum boundary of 1 and does not decrement further.
 *
 * Scope restrictions honoured:
 *   - #planBtn is never clicked.
 *   - The form is never submitted.
 *   - generatePlan() is never invoked.
 *   - No network calls (/api/plan, Groq, wttr.in).
 *   - No modifications to the original application or other tests.
 */
public class TravelPlannerTravelerCountTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("Traveler Counter: Increments, decrements, and enforces minimum count of 1")
    void travelerCounterIncrementsDecrementsAndEnforcesMinimum() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true);

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // 1. Navigate to the application home page
                page.navigate(APP_URL);

                // 2. Identify the counter element and increment/decrement buttons
                Locator travelerCount = page.locator("#travelerCount");
                Locator decrementBtn = page.locator(".traveler-counter button.counter-btn").filter(new Locator.FilterOptions().setHasText("-"));
                Locator incrementBtn = page.locator(".traveler-counter button.counter-btn").filter(new Locator.FilterOptions().setHasText("+"));

                // Verify elements exist and are visible
                assertTrue(travelerCount.isVisible(), "Traveler count (#travelerCount) should be visible");
                assertTrue(decrementBtn.isVisible(), "Decrement button should be visible");
                assertTrue(incrementBtn.isVisible(), "Increment button should be visible");

                // 3. Verify initial traveler count is 2
                assertEquals("2", travelerCount.textContent().trim(),
                        "Initial traveler count should be 2");

                // 4. Click increment (+) and verify count becomes 3
                incrementBtn.click();
                assertEquals("3", travelerCount.textContent().trim(),
                        "Traveler count should be 3 after clicking increment (+)");

                // 5. Click decrement (-) twice and verify count becomes 1
                decrementBtn.click();
                decrementBtn.click();
                assertEquals("1", travelerCount.textContent().trim(),
                        "Traveler count should be 1 after decrementing twice");

                // 6. Click decrement (-) once more and verify count remains 1
                decrementBtn.click();
                assertEquals("1", travelerCount.textContent().trim(),
                        "Traveler count should remain 1 when decrementing past the minimum boundary");
            }
        }
    }
}
