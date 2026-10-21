package com.travelplanner.automation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Task 9 — Mocked API Workflow Test for Itinerary Generation.
 *
 * Verifies the end-to-end user workflow from filling the travel form on the home page,
 * submitting via #planBtn, intercepting the POST /api/plan HTTP request, and fulfilling
 * it with a deterministic mock response without making real backend, Groq, or weather API calls.
 *
 * Then verifies proper navigation to /itinerary.html and checks that the dashboard
 * rendered the mocked data successfully.
 */
public class TravelPlannerMockedPlanGenerationTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("Mocked Plan Generation: Intercepts POST /api/plan, returns mock itinerary, and verifies dashboard navigation")
    void shouldGeneratePlanWithMockedApiResponseAndNavigateToItinerary() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true);

            try (Browser browser = playwright.chromium().launch(launchOptions);
                 Page page = browser.newPage()) {

                // 1. Navigate to the application home page
                page.navigate(APP_URL);

                // Tracking objects to verify route interception details
                AtomicBoolean routeIntercepted = new AtomicBoolean(false);
                AtomicReference<String> interceptedMethod = new AtomicReference<>();
                AtomicReference<String> interceptedPostData = new AtomicReference<>();

                // Mock response JSON strictly matching TravelController response structure
                String mockResponseJson = """
                        {
                          "destination": "Paris",
                          "weather": "Paris: 🌤️ +22°C",
                          "itinerary": "## Day 1: Welcome to Paris\\n\\nExplore the Louvre and the Eiffel Tower."
                        }
                        """;

                // 2. Register route interception for POST **/api/plan BEFORE initiating submission
                page.route("**/api/plan", route -> {
                    routeIntercepted.set(true);
                    interceptedMethod.set(route.request().method());
                    interceptedPostData.set(route.request().postData());

                    route.fulfill(new Route.FulfillOptions()
                            .setStatus(200)
                            .setContentType("application/json")
                            .setBody(mockResponseJson));
                });

                // 3. Fill required travel form fields to satisfy client-side generatePlan() validation
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

                // 4. Click the Plan button to submit
                Locator planBtn = page.locator("#planBtn");
                planBtn.click();

                // 5. Wait for navigation to the itinerary dashboard page (/itinerary.html)
                // The app waits 800ms before setting window.location.href = '/itinerary.html'
                page.waitForURL(url -> url.contains("itinerary.html"));

                // 6. Assertions on the intercepted request
                assertTrue(routeIntercepted.get(), "POST /api/plan route must have been intercepted");
                assertEquals("POST", interceptedMethod.get(), "Intercepted request method must be POST");

                String postData = interceptedPostData.get();
                assertNotNull(postData, "Intercepted request postData must not be null");
                assertTrue(postData.contains("\"destination\""), "Request payload must contain 'destination'");
                assertTrue(postData.contains("\"duration\""), "Request payload must contain 'duration'");
                assertTrue(postData.contains("\"budget\""), "Request payload must contain 'budget'");
                assertTrue(postData.contains("\"interests\""), "Request payload must contain 'interests'");
                assertTrue(postData.contains("Paris"), "Request destination must contain 'Paris'");

                // 7. Assertions on the rendered itinerary dashboard
                assertTrue(page.url().contains("itinerary.html"), "Browser URL must be on the itinerary dashboard page");

                Locator summaryDestination = page.locator("#summaryDestination");
                assertTrue(summaryDestination.isVisible(), "Summary destination element should be visible on itinerary page");
                assertTrue(summaryDestination.textContent().contains("Paris"),
                        "Summary destination should contain 'Paris'");

                Locator snapshotLocation = page.locator("#snapshotLocation");
                assertTrue(snapshotLocation.isVisible(), "Snapshot location element should be visible on itinerary page");
                assertEquals("Paris", snapshotLocation.textContent().trim(),
                        "Snapshot location should display 'Paris'");

                Locator exportBtn = page.locator("#exportPdfBtn");
                assertTrue(exportBtn.isVisible(), "Export PDF button should be visible on the loaded itinerary page");
            }
        }
    }
}
