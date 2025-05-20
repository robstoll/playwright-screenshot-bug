package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.Map;

public class FooTest {
    @Test
    void screenshot() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            String url = "https://google.com";
            page.route(
                    u -> u.startsWith(url),
                    route -> {
                        APIResponse response = route.fetch();
                        Map<String, String> headers = new HashMap<>(response.headers());
                        headers.put("Content-Security-Policy", "default-src " + url + ";");
                        route.fulfill(
                                new Route.FulfillOptions()
                                        .setResponse(response)
                                        .setBodyBytes(response.body())
                                        .setHeaders(headers)
                        );
                    }
            );
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            page.onConsoleMessage(msg -> {
                if (atomicBoolean.getAndSet(false)) {
                    // Simulate taking a screenshot on test failure (usually in a TestWatcher)
                    page.screenshot(new Page.ScreenshotOptions().setFullPage(true).setPath(Paths.get("test.png")));
                    // You can also replace the above with the following, same behaviour
                    // page.waitForLoadState();
                }
            });
            page.navigate(url);
        }
    }

}
