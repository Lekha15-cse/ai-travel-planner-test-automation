package com.travelplanner.automation.extensions;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;

/**
 * JUnit 5 extension that automatically captures a browser screenshot when a test fails.
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>{@code beforeEach} — Creates Playwright, launches a headless Chromium browser,
 *       and opens a new Page. Resources are stored in the JUnit
 *       {@link ExtensionContext.Store} for the test's lifetime.</li>
 *   <li><strong>Test method</strong> — Receives the {@link Page} via JUnit 5 parameter
 *       resolution. All navigation, interactions, and assertions happen here.</li>
 *   <li>{@code afterTestExecution} — Runs <em>after</em> the test method but
 *       <em>before</em> resource cleanup. If the test failed, a full-page screenshot
 *       and a companion failure-details file are saved to
 *       {@code target/test-evidence/screenshots/}.</li>
 *   <li>{@code afterEach} — Closes Page, Browser, and Playwright in order.</li>
 * </ol>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @ExtendWith(ScreenshotOnFailureExtension.class)
 * class MyTest {
 *     @Test
 *     void exampleTest(Page page) {
 *         page.navigate("http://localhost:8081/");
 *         assertTrue(page.locator("h1").isVisible());
 *     }
 * }
 * }</pre>
 *
 * <h3>Design notes</h3>
 * <ul>
 *   <li>Resources are only created when the test method declares a {@link Page}
 *       parameter, so non-Playwright tests (e.g. pure JUnit smoke tests) are
 *       unaffected.</li>
 *   <li>Screenshot capture is wrapped in a try-catch so that failures during
 *       capture never mask the original test error.</li>
 * </ul>
 */
public class ScreenshotOnFailureExtension implements
        BeforeEachCallback, AfterTestExecutionCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(ScreenshotOnFailureExtension.class);

    private static final String KEY_PLAYWRIGHT = "playwright";
    private static final String KEY_BROWSER = "browser";
    private static final String KEY_PAGE = "page";

    // ---------------------------------------------------------------
    //  Lifecycle callbacks
    // ---------------------------------------------------------------

    @Override
    public void beforeEach(ExtensionContext context) {
        if (!testMethodRequiresPage(context)) {
            return;
        }

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true));
        Page page = browser.newPage();

        ExtensionContext.Store store = context.getStore(NAMESPACE);
        store.put(KEY_PLAYWRIGHT, playwright);
        store.put(KEY_BROWSER, browser);
        store.put(KEY_PAGE, page);
    }

    /**
     * Invoked after the test method returns (pass or fail) but before
     * {@link #afterEach} closes the browser. If the test failed,
     * the Page is still alive and a screenshot can be captured.
     */
    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return; // Test passed — no screenshot needed
        }

        ExtensionContext.Store store = context.getStore(NAMESPACE);
        Page page = store.get(KEY_PAGE, Page.class);

        if (page == null || page.isClosed()) {
            return;
        }

        Throwable failure = context.getExecutionException().get();
        String testClassName = context.getRequiredTestClass().getSimpleName();
        String testMethodName = context.getRequiredTestMethod().getName();

        ScreenshotUtil.captureScreenshot(page, testClassName, testMethodName, failure.getMessage());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);

        Page page = store.remove(KEY_PAGE, Page.class);
        if (page != null && !page.isClosed()) {
            page.close();
        }

        Browser browser = store.remove(KEY_BROWSER, Browser.class);
        if (browser != null && browser.isConnected()) {
            browser.close();
        }

        Playwright playwright = store.remove(KEY_PLAYWRIGHT, Playwright.class);
        if (playwright != null) {
            playwright.close();
        }
    }

    // ---------------------------------------------------------------
    //  Parameter resolution
    // ---------------------------------------------------------------

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return Page.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(KEY_PAGE, Page.class);
    }

    // ---------------------------------------------------------------
    //  Helpers
    // ---------------------------------------------------------------

    private boolean testMethodRequiresPage(ExtensionContext context) {
        for (Parameter param : context.getRequiredTestMethod().getParameters()) {
            if (Page.class.isAssignableFrom(param.getType())) {
                return true;
            }
        }
        return false;
    }
}
