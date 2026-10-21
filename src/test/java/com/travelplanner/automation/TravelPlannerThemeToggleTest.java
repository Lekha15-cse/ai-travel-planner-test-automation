package com.travelplanner.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UI automation test for the AI Travel Planner theme toggle functionality.
 *
 * Verifies that clicking the theme toggle button:
 *   - Inverts the 'dark-mode' CSS class on the <html> root element.
 *   - Restores the original theme state upon clicking a second time.
 *
 * Scope restrictions honoured:
 *   - #planBtn is never clicked.
 *   - The travel form is never submitted.
 *   - generatePlan() is never invoked.
 *   - No network calls (/api/plan, Groq, wttr.in).
 *   - No modifications to the original application or other tests.
 */
public class TravelPlannerThemeToggleTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("Theme Toggle: Toggles 'dark-mode' class on <html> and restores original state")
    void themeToggleSwitchesDarkModeAndRestoresOriginalState() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true);

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // 1. Navigate to the application home page
                page.navigate(APP_URL);

                // 2. Establish a known starting state independent of previous preferences
                // Clear localStorage and re-initialize theme to ensure clean baseline
                page.evaluate("() => { localStorage.clear(); if (typeof initTheme === 'function') { initTheme(); } }");

                // 3. Locate the theme toggle button using its ID selector
                Locator themeToggle = page.locator("#themeToggle");
                assertTrue(themeToggle.isVisible(), "Theme toggle button (#themeToggle) should be visible");

                // 4. Record the initial state of the <html> element's 'dark-mode' class
                boolean initiallyDark = (Boolean) page.evaluate("() => document.documentElement.classList.contains('dark-mode')");

                // 5. Click the theme toggle once
                themeToggle.click();

                // 6. Assert that the 'dark-mode' class changed
                boolean afterFirstToggle = (Boolean) page.evaluate("() => document.documentElement.classList.contains('dark-mode')");
                assertNotEquals(initiallyDark, afterFirstToggle,
                        "The <html> element's 'dark-mode' class should change state after the first click");
                assertEquals(!initiallyDark, afterFirstToggle,
                        "The <html> element's 'dark-mode' class should be inverted after the first click");

                // 7. Click the theme toggle again
                themeToggle.click();

                // 8. Assert that the original theme state is restored
                boolean afterSecondToggle = (Boolean) page.evaluate("() => document.documentElement.classList.contains('dark-mode')");
                assertEquals(initiallyDark, afterSecondToggle,
                        "The <html> element's 'dark-mode' class should restore to its initial state after the second click");
            }
        }
    }
}
