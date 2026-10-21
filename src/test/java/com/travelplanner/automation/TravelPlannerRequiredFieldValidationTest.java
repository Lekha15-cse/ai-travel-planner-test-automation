package com.travelplanner.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step 3C, Task 4 — Required Field Validation.
 *
 * Verifies that the #destination input carries the HTML required attribute and
 * that the browser's built-in Constraint Validation API correctly reports
 * valueMissing when the field is empty and validity when it is filled.
 *
 * Scope restrictions honoured:
 *   - #planBtn is never clicked.
 *   - The form is never submitted.
 *   - generatePlan() is never invoked.
 *   - No toast / notification / loading assertions.
 *   - No network calls (api/plan, Groq, wttr.in).
 *   - All checks are read-only Playwright evaluations against the DOM.
 */
public class TravelPlannerRequiredFieldValidationTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("Required Field: #destination has required attribute; empty fails validity, filled passes validity")
    void destinationRequiredAttributeAndValidityCheck() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true);

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // Step 1 — Navigate to the home page
                page.navigate(APP_URL);

                // Step 2 — Locate #destination
                Locator destination = page.locator("#destination");

                // Step 3 — Assert the element exists
                assertTrue(destination.count() > 0,
                        "#destination input must be present on the page");

                // Step 4 — Assert the required attribute is present
                String requiredAttr = destination.getAttribute("required");
                assertNotNull(requiredAttr,
                        "#destination must have the HTML 'required' attribute");

                // Step 5 — Clear the field so it is empty
                destination.fill("");

                // Step 6 — Empty field: checkValidity() must return false
                Boolean validWhenEmpty = (Boolean) destination.evaluate("el => el.checkValidity()");
                assertFalse(validWhenEmpty,
                        "checkValidity() must return false when #destination is empty");

                // Step 7 — Empty field: validity.valueMissing must be true
                Boolean valueMissing = (Boolean) destination.evaluate("el => el.validity.valueMissing");
                assertTrue(valueMissing,
                        "validity.valueMissing must be true when #destination is empty");

                // Step 8 — Fill the field with a valid value
                destination.fill("Paris");

                // Step 9 — Filled field: checkValidity() must return true
                Boolean validWhenFilled = (Boolean) destination.evaluate("el => el.checkValidity()");
                assertTrue(validWhenFilled,
                        "checkValidity() must return true when #destination contains 'Paris'");
            }
        }
    }
}