package com.travelplanner.automation;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.travelplanner.automation.extensions.ScreenshotOnFailureExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UI automation test for the AI Travel Planner PDF export functionality.
 *
 * Verifies that on the itinerary dashboard (/itinerary.html):
 *   - Pre-seeded sessionStorage data correctly renders the itinerary dashboard.
 *   - The Export PDF button (#exportPdfBtn) is visible and enabled.
 *   - Clicking the Export PDF button triggers a client-side PDF document generation
 *     using jsPDF and initiates a browser download of a non-empty .pdf file.
 *   - A success notification is displayed to confirm the export.
 *
 * Scope restrictions honoured:
 *   - Real backend, Groq AI, and weather APIs are never invoked.
 *   - Operates purely in-browser on deterministic pre-seeded sessionStorage data.
 *   - Original application files are not modified.
 */
@ExtendWith(ScreenshotOnFailureExtension.class)
public class TravelPlannerPdfExportTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Test
    @DisplayName("PDF Export: Clicking export button generates and downloads non-empty PDF report")
    void shouldExportItineraryAsPdfDownload(Page page) throws IOException {
        // 1. Establish the origin and seed sessionStorage with valid travel data
        page.navigate(APP_URL);

                String sampleTravelDataJson = """
                        {
                          "destination": "Paris (Flying from: New York, JFK)",
                          "duration": "5 days (for 2 travelers)",
                          "budget": "₹85000",
                          "interests": "Food, Culture",
                          "weather": "Paris: 🌤️ +22°C",
                          "itinerary": "### Day 1: Arrival & Exploration\\n\\n**Morning:** Check into your hotel in central Paris.\\n\\n**Afternoon:** Visit the Louvre Museum.\\n\\n**Evening:** Dine at a traditional French bistro.\\n\\n### Day 2: Historic Landmarks\\n\\n**Morning:** Tour the Eiffel Tower and Champ de Mars.\\n\\n**Afternoon:** Cruise along the Seine River.\\n\\n**Evening:** Walk through Montmartre."
                        }
                        """;

                page.evaluate("data => sessionStorage.setItem('travelData', data)", sampleTravelDataJson);

                // 2. Navigate directly to the itinerary dashboard page
                page.navigate(APP_URL + "itinerary.html");

                // 3. Verify that the dashboard loaded with the pre-seeded data
                Locator summaryDestination = page.locator("#summaryDestination");
                assertTrue(summaryDestination.isVisible(), "Summary destination should be visible on dashboard");
                assertTrue(summaryDestination.textContent().contains("Paris"),
                        "Summary destination should display 'Paris'");

                // 4. Verify that the Export PDF button is visible and enabled
                Locator exportPdfBtn = page.locator("#exportPdfBtn");
                assertTrue(exportPdfBtn.isVisible(), "Export PDF button (#exportPdfBtn) should be visible");
                assertTrue(exportPdfBtn.isEnabled(), "Export PDF button (#exportPdfBtn) should be enabled");

                // 5. Click the Export button and listen for the browser download event
                Download download = page.waitForDownload(exportPdfBtn::click);

                // 6. Assert download properties
                assertNotNull(download, "A download event must be triggered upon clicking the Export PDF button");

                String suggestedFilename = download.suggestedFilename();
                assertNotNull(suggestedFilename, "Downloaded file must have a filename");
                assertTrue(suggestedFilename.endsWith(".pdf"),
                        "Downloaded file extension must be .pdf, actual filename: " + suggestedFilename);
                assertTrue(suggestedFilename.contains("Paris"),
                        "Downloaded filename should mention the destination 'Paris', actual: " + suggestedFilename);

                // 7. Assert that the downloaded PDF file is saved and non-empty
                Path downloadPath = download.path();
                assertNotNull(downloadPath, "Downloaded temporary file path should not be null");
                long fileSize = Files.size(downloadPath);
                assertTrue(fileSize > 0, "Downloaded PDF file must not be empty (file size was " + fileSize + " bytes)");

                // 8. Verify the file starts with the PDF signature (%PDF-)
                byte[] header = new byte[5];
                try (FileInputStream fis = new FileInputStream(downloadPath.toFile())) {
                    int bytesRead = fis.read(header);
                    assertEquals(5, bytesRead, "Should be able to read at least 5 bytes from the PDF file");
                }
                String headerString = new String(header, java.nio.charset.StandardCharsets.US_ASCII);
                assertEquals("%PDF-", headerString,
                        "Downloaded file should begin with the PDF signature '%PDF-', actual: " + headerString);

                // 9. Assert that the application displayed the success notification
                Locator successNotification = page.locator(".notification.notification-success");
                assertTrue(successNotification.isVisible(), "Success notification should be visible after export");
                assertTrue(successNotification.textContent().contains("Professional Report exported successfully!"),
                        "Notification text should confirm successful export");
    }
}
