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
 * Step 3C, Task 5 — Valid Travel-Form Input Behavior.
 *
 * Verifies that valid form inputs across all key fields (departure, destination,
 * travel dates, budget, and interest chips) are accepted, properly validated by
 * HTML5 constraint validation, and synchronized to client-side UI indicators
 * and hidden payload fields.
 *
 * Scope restrictions honoured:
 *   - #planBtn is never clicked.
 *   - The form is never submitted.
 *   - generatePlan() is never invoked.
 *   - No network calls (/api/plan, Groq, wttr.in).
 *   - No assertions on generated itineraries or API responses.
 *   - All interactions and assertions are strictly confined to the client DOM.
 */
public class TravelPlannerValidFormInputTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("Valid Form Input: Form inputs are accepted, validated, and synchronized without submitting")
    void validTravelFormInputIsAcceptedAndSynchronized() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true);

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // 1. Navigate to application home page
                page.navigate(APP_URL);

                // Define locators for form fields and UI feedback elements
                Locator flyingFrom = page.locator("#flyingFrom");
                Locator destination = page.locator("#destination");
                Locator startDate = page.locator("#startDate");
                Locator endDate = page.locator("#endDate");
                Locator durationDisplay = page.locator("#durationDisplay");
                Locator hiddenDuration = page.locator("#duration");
                Locator budgetInput = page.locator("#budgetInput");
                Locator hiddenBudget = page.locator("#budget");
                Locator budgetWarning = page.locator("#budgetWarning");
                Locator cultureChip = page.locator("#interestsContainer button.chip").filter(new Locator.FilterOptions().setHasText("Culture"));
                Locator foodChip = page.locator("#interestsContainer button.chip").filter(new Locator.FilterOptions().setHasText("Food"));
                Locator hiddenInterests = page.locator("#interests");
                Locator planBtn = page.locator("#planBtn");

                // 2. Fill departure: #flyingFrom with 'New York, JFK'
                flyingFrom.fill("New York, JFK");

                // 3. Fill destination: #destination with 'Paris'
                destination.fill("Paris");

                // 4. Fill start date: #startDate with '2026-11-01'
                startDate.fill("2026-11-01");

                // 5. Fill end date: #endDate with '2026-11-05'
                endDate.fill("2026-11-05");

                // 6. Dispatch date change events to run updateDuration() logic
                startDate.dispatchEvent("change");
                endDate.dispatchEvent("change");

                // 7. Fill budget: #budgetInput with '85000'
                budgetInput.fill("85000");

                // 8. Blur #budgetInput so budget synchronization and heuristic logic run
                budgetInput.blur();

                // 9. Select the Culture interest chip
                cultureChip.click();

                // 10. Select the Food interest chip
                foodChip.click();

                // 11. Assert departure value equals 'New York, JFK'
                assertEquals("New York, JFK", flyingFrom.inputValue(),
                        "Departure input (#flyingFrom) value should be 'New York, JFK'");

                // 12. Assert departure HTML5 validity returns true
                Boolean flyingFromValid = (Boolean) flyingFrom.evaluate("el => el.checkValidity()");
                assertTrue(flyingFromValid,
                        "Departure input (#flyingFrom) must satisfy HTML5 constraint validation");

                // 13. Assert destination value equals 'Paris'
                assertEquals("Paris", destination.inputValue(),
                        "Destination input (#destination) value should be 'Paris'");

                // 14. Assert destination HTML5 validity returns true
                Boolean destinationValid = (Boolean) destination.evaluate("el => el.checkValidity()");
                assertTrue(destinationValid,
                        "Destination input (#destination) must satisfy HTML5 constraint validation");

                // 15. Assert start date value is '2026-11-01'
                assertEquals("2026-11-01", startDate.inputValue(),
                        "Start date (#startDate) value should be '2026-11-01'");

                // 16. Assert end date value is '2026-11-05'
                assertEquals("2026-11-05", endDate.inputValue(),
                        "End date (#endDate) value should be '2026-11-05'");

                // 17. Assert #durationDisplay contains '5 days total'
                assertTrue(durationDisplay.textContent().contains("5 days total"),
                        "Duration display (#durationDisplay) should contain '5 days total'");

                // 18. Assert hidden #duration has value '5 days'
                assertEquals("5 days", hiddenDuration.inputValue().trim(),
                        "Hidden duration input (#duration) should have value '5 days'");

                // 19. Assert #budgetInput has value '85000'
                assertEquals("85000", budgetInput.inputValue(),
                        "Budget input (#budgetInput) should have value '85000'");

                // 20. Assert hidden #budget has value '₹85000'
                assertEquals("₹85000", hiddenBudget.inputValue(),
                        "Hidden budget input (#budget) should have value '₹85000'");

                // 21. Assert #budgetWarning is not visible
                assertFalse(budgetWarning.isVisible(),
                        "Budget warning (#budgetWarning) should not be visible for an adequate budget");

                // 22. Assert Culture chip is active
                String cultureClass = cultureChip.getAttribute("class");
                assertNotNull(cultureClass, "Culture chip must have class attribute");
                assertTrue(cultureClass.contains("active"),
                        "Culture interest chip should have 'active' class");

                // 23. Assert Food chip is active
                String foodClass = foodChip.getAttribute("class");
                assertNotNull(foodClass, "Food chip must have class attribute");
                assertTrue(foodClass.contains("active"),
                        "Food interest chip should have 'active' class");

                // 24. Assert hidden #interests contains both Culture and Food
                String interestsValue = hiddenInterests.inputValue();
                assertTrue(interestsValue.contains("Culture"),
                        "Hidden interests input (#interests) should contain 'Culture'");
                assertTrue(interestsValue.contains("Food"),
                        "Hidden interests input (#interests) should contain 'Food'");

                // 25. Assert #planBtn is visible
                assertTrue(planBtn.isVisible(),
                        "Plan button (#planBtn) should be visible");

                // 26. Assert #planBtn is enabled
                assertTrue(planBtn.isEnabled(),
                        "Plan button (#planBtn) should be enabled");
            }
        }
    }
}
