package com.travelplanner.automation;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.travelplanner.automation.extensions.ScreenshotOnFailureExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic Playwright browser verification test.
 * Verifies that Playwright can launch a Chromium headless browser,
 * render HTML content via a data URL, and interact with/assert on elements.
 */
@ExtendWith(ScreenshotOnFailureExtension.class)
public class PlaywrightBrowserSmokeTest {

    @Test
    @DisplayName("Playwright: Launch Chromium, navigate to local HTML data URL, and verify heading visibility")
    void shouldLaunchBrowserAndVerifyHeading(Page page) {
        // Load self-contained local HTML data URL (no external web or app dependencies)
        String htmlDataUrl = "data:text/html,<html><body><h1 id='main-heading'>Welcome to Travel Automation</h1></body></html>";
        page.navigate(htmlDataUrl);

        // Locate the heading element
        Locator heading = page.locator("#main-heading");

        // Assertions
        assertTrue(heading.isVisible(), "Heading should be rendered and visible in the browser");
        assertEquals("Welcome to Travel Automation", heading.textContent().trim(),
                "Heading text should match expected content");
    }
}
