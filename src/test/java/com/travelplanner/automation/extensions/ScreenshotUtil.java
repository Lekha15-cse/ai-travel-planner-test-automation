package com.travelplanner.automation.extensions;

import com.microsoft.playwright.Page;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for capturing failure screenshots and writing structured bug-evidence metadata.
 *
 * <p>Screenshots are saved to {@code target/test-evidence/screenshots/} with filenames
 * encoding the test class, method name, and timestamp. A companion {@code _details.txt}
 * file captures structured failure information for triage.</p>
 *
 * <p>All exceptions during capture are caught and logged to {@code stderr},
 * ensuring the original test failure is never masked.</p>
 */
public final class ScreenshotUtil {

    private static final Path SCREENSHOT_DIR =
            Paths.get("target", "test-evidence", "screenshots");

    private static final DateTimeFormatter TIMESTAMP_FILENAME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private static final DateTimeFormatter TIMESTAMP_READABLE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private ScreenshotUtil() {
        // Utility class — prevent instantiation
    }

    /**
     * Captures a full-page screenshot and writes a failure-details text file.
     *
     * @param page           the Playwright {@link Page} to screenshot (must still be open)
     * @param testClassName  simple name of the test class
     * @param testMethodName name of the failed test method
     * @param failureMessage the assertion or error message from the failure
     */
    public static void captureScreenshot(Page page, String testClassName,
                                         String testMethodName, String failureMessage) {
        try {
            Files.createDirectories(SCREENSHOT_DIR);

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FILENAME);
            String baseFilename = testClassName + "_" + testMethodName + "_" + timestamp;

            // 1. Capture full-page screenshot as PNG
            Path screenshotPath = SCREENSHOT_DIR.resolve(baseFilename + ".png");
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true));

            // 2. Write structured failure details alongside the screenshot
            Path detailsPath = SCREENSHOT_DIR.resolve(baseFilename + "_details.txt");
            String details = String.join(System.lineSeparator(),
                    "=== Test Failure Evidence ===",
                    "",
                    "Test Class:      " + testClassName,
                    "Test Method:     " + testMethodName,
                    "Failure Message: " + (failureMessage != null ? failureMessage : "N/A"),
                    "Timestamp:       " + LocalDateTime.now().format(TIMESTAMP_READABLE),
                    "Screenshot:      " + screenshotPath.toAbsolutePath(),
                    ""
            );
            Files.writeString(detailsPath, details);

            System.err.println("[SCREENSHOT] Failure evidence saved: " + screenshotPath.toAbsolutePath());

        } catch (Exception e) {
            // Gracefully handle screenshot failures — never hide the original test error
            System.err.println("[SCREENSHOT] WARNING: Could not capture failure screenshot: " + e.getMessage());
        }
    }
}
