package com.travelplanner.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UI automation test for the AI Travel Planner API failure handling.
 *
 * Verifies that when the POST /api/plan endpoint returns an HTTP 500 error:
 *   - The error notification (.notification-error) becomes visible with the expected message.
 *   - The travel form (#travelForm) is restored and visible.
 *   - The loading screen (#loading) is hidden.
 *   - The Plan button (#planBtn) is re-enabled.
 *   - The browser remains on the home page and does not navigate to itinerary.html.
 *
 * Scope restrictions honoured:
 *   - Real backend, Groq AI, and weather APIs are never called.
 *   - Network requests to /api/plan are intercepted and mocked with HTTP 500.
 *   - Original application files are not modified.
 */
public class TravelPlannerApiFailureHandlingTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("API Failure: HTTP 500 error shows error notification, restores form, and enables Plan button")
    void shouldHandleApi500ErrorGracefully() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true);

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // 1. Open the home page
                page.navigate(APP_URL);

                // 2. Register route handler for **/api/plan to return HTTP 500 before submitting
                page.route("**/api/plan", route -> {
                    route.fulfill(new Route.FulfillOptions()
                            .setStatus(500)
                            .setContentType("application/json")
                            .setBody("{\"error\": \"Simulated internal server error\"}"));
                });

                // 3. Fill required form fields with valid values
                Locator flyingFrom = page.locator("#flyingFrom");
                Locator destination = page.locator("#destination");
                Locator startDate = page.locator("#startDate");
                Locator endDate = page.locator("#endDate");
                Locator budgetInput = page.locator("#budgetInput");
                Locator foodChip = page.locator("#interestsContainer button.chip")
                        .filter(new Locator.FilterOptions().setHasText("Food"));

                flyingFrom.fill("New York, JFK");
                destination.fill("Paris");
                startDate.fill("2026-11-01");
                endDate.fill("2026-11-05");
                endDate.dispatchEvent("change");
                budgetInput.fill("85000");
                budgetInput.blur();
                foodChip.click();

                // 4. Click the Plan button to trigger submission
                Locator planBtn = page.locator("#planBtn");
                planBtn.click();

                // 5. Wait for the error notification to become visible
                Locator errorNotification = page.locator(".notification.notification-error");
                errorNotification.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

                // 6. Assert error notification visibility and expected text
                assertTrue(errorNotification.isVisible(),
                        "Error notification (.notification-error) should be visible upon API 500 failure");
                assertTrue(errorNotification.textContent().contains("Failed to generate travel plan. Please try again."),
                        "Error notification should contain expected failure message");

                // 7. Assert that the travel form is visible again
                Locator travelForm = page.locator("#travelForm");
                assertTrue(travelForm.isVisible(),
                        "Travel form (#travelForm) should be visible again after API failure");

                // 8. Assert that the loading screen is hidden
                Locator loading = page.locator("#loading");
                assertFalse(loading.isVisible(),
                        "Loading indicator (#loading) should be hidden after API failure");

                // 9. Assert that the Plan button is re-enabled and visible
                assertTrue(planBtn.isEnabled(),
                        "Plan button (#planBtn) should be re-enabled after API failure");
                assertTrue(planBtn.isVisible(),
                        "Plan button (#planBtn) should remain visible after API failure");

                // 10. Assert that the browser remains on the home page and did not navigate away
                assertFalse(page.url().contains("itinerary.html"),
                        "Browser URL should not navigate to itinerary.html on API failure");
                assertTrue(page.url().contains("8081"),
                        "Browser should remain on the home page URL");
            }
        }
    }
}
