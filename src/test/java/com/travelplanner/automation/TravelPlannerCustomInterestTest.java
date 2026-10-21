package com.travelplanner.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 3C, Task 7 — Custom Interest Chip UI Automation Test.
 *
 * Verifies that typing a custom interest into #customInterest and pressing
 * the Enter key:
 *   - Creates a new dynamic chip button within #interestsContainer.
 *   - The newly created chip is visible and has the 'active' class.
 *   - The hidden #interests input is synchronized to include the custom interest.
 *
 * Scope restrictions honoured:
 *   - #planBtn is never clicked.
 *   - The travel form is never submitted.
 *   - No network calls (/api/plan, Groq, wttr.in).
 *   - No modifications to the original application or other tests.
 */
public class TravelPlannerCustomInterestTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("Custom Interest: Dynamic chip is added and synchronized via #customInterest Enter keypress")
    void customInterestIsAddedAndSynchronized() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true);

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // 1. Navigate to home page
                page.navigate(APP_URL);

                // 2. Locate the custom-interest input field
                Locator customInterestInput = page.locator("#customInterest");
                assertTrue(customInterestInput.isVisible(), "Custom interest input (#customInterest) should be visible");

                // 3. Enter 'Photography' into the custom interest input
                customInterestInput.fill("Photography");

                // 4. Trigger the custom interest addition via Enter keypress
                customInterestInput.press("Enter");

                // 5. Locate the newly created chip button inside #interestsContainer
                Locator newChip = page.locator("#interestsContainer button.chip")
                        .filter(new Locator.FilterOptions().setHasText("Photography"));

                // 6. Verify that the newly created chip is visible and selected ('active' class)
                assertTrue(newChip.isVisible(), "Newly created 'Photography' chip should be visible in #interestsContainer");

                String chipClass = newChip.getAttribute("class");
                assertNotNull(chipClass, "New chip should have a class attribute");
                assertTrue(chipClass.contains("active"), "New 'Photography' chip should have the 'active' class");

                // 7. Verify that the hidden #interests input contains 'Photography'
                Locator hiddenInterests = page.locator("#interests");
                String interestsValue = hiddenInterests.inputValue();
                assertTrue(interestsValue.contains("Photography"),
                        "Hidden #interests input value should contain 'Photography', actual value: " + interestsValue);
            }
        }
    }
}
