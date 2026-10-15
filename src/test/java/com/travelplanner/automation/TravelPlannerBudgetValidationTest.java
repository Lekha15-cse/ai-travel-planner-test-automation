package com.travelplanner.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UI validation test verifying client-side budget heuristics.
 * Ensures an advisory warning appears when the entered budget is below the
 * destination's minimum estimated cost, and disappears once an adequate budget is supplied.
 */
public class TravelPlannerBudgetValidationTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("Budget Validation: Low budget displays advisory warning, sufficient budget hides it")
    void shouldDisplayWarningForLowBudgetAndHideWhenBudgetSufficient() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true);

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // 1. Navigate to home page
                page.navigate(APP_URL);

                Locator destinationInput = page.locator("#destination");
                Locator budgetInput = page.locator("#budgetInput");
                Locator budgetWarning = page.locator("#budgetWarning");

                // Test A: Low budget displays warning
                destinationInput.fill("Paris");
                budgetInput.fill("5000");
                budgetInput.blur();

                assertTrue(budgetWarning.isVisible(),
                        "Budget warning should be visible when budget is below heuristic threshold for Paris");
                assertTrue(budgetWarning.textContent().contains("Trips to Paris generally start around ₹80,000"),
                        "Budget warning should advise that trips to Paris generally start around ₹80,000");

                // Test B: Sufficient budget hides warning
                budgetInput.fill("85000");
                budgetInput.blur();

                assertFalse(budgetWarning.isVisible(),
                        "Budget warning should be hidden when budget is increased to ₹85,000");
            }
        }
    }
}
