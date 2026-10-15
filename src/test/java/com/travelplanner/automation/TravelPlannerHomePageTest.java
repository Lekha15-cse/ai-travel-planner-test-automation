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
 * Real UI automation test for the AI Travel Planner web application.
 * Verifies that the home page loads, the title/heading render properly,
 * and key form controls (such as #destination and #planBtn) are visible,
 * enabled, and accept user input without submitting the form.
 */
public class TravelPlannerHomePageTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("Home Page: Verify page title, hero heading, destination input, and plan button")
    void shouldLoadHomePageAndVerifyFormControls() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true);

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // 1. Navigate to the local application home page
                page.navigate(APP_URL);

                // 2. Verify page title matches index.html definition
                String expectedTitle = "AI Travel Planner - Plan Your Perfect Trip";
                assertEquals(expectedTitle, page.title(), "Page title should match index.html title tag");

                // 3. Verify the main hero heading
                Locator heroHeading = page.locator("h1.hero-title");
                assertTrue(heroHeading.isVisible(), "Hero heading should be visible");
                assertEquals("Plan Your Perfect Trip with AI", heroHeading.textContent().trim(),
                        "Hero heading text should match expected text");

                // 4. Verify destination input field is visible and enabled
                Locator destinationInput = page.locator("#destination");
                assertTrue(destinationInput.isVisible(), "Destination input field (#destination) should be visible");
                assertTrue(destinationInput.isEnabled(), "Destination input field (#destination) should be enabled");

                // 5. Enter 'Paris' into destination and verify its value
                destinationInput.fill("Paris");
                assertEquals("Paris", destinationInput.inputValue(),
                        "Destination input field value should equal 'Paris' after typing");

                // 6. Verify the Plan button is visible and enabled (Do not click)
                Locator planBtn = page.locator("#planBtn");
                assertTrue(planBtn.isVisible(), "Plan button (#planBtn) should be visible");
                assertTrue(planBtn.isEnabled(), "Plan button (#planBtn) should be enabled");
            }
        }
    }
}
