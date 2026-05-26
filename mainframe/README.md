# `mainframe` — Selenium-style automation façade over tn5250j

A small Java module that wraps [tn5250j](https://github.com/tn5250j/tn5250j) with a
WebDriver-flavoured API for automating AS/400 / IBM i green screens.

## Quick start

```java
import java.time.Duration;
import com.bns.etbic.craft.mainframe.MainframeDriver;
import com.bns.etbic.craft.mainframe.keys.Key;
import com.bns.etbic.craft.mainframe.locators.By;
import com.bns.etbic.craft.mainframe.waits.MainframeConditions;

try (MainframeDriver driver = MainframeDriver.builder()
        .host("as400.example.com").port(23)
        .codePage("37")
        .deviceName("WS00001")            // required — host disconnects without it
        .headless(true)                   // false opens a Swing window with the live screen
        .defaultTimeout(Duration.ofSeconds(30))
        .build()) {

    driver.connect();
    driver.waitFor(MainframeConditions.inputReady());

    driver.findField(By.labelLeftOf("User .:")).type("MYUSER");
    driver.findField(By.labelLeftOf("Password")).type("S3cret!");
    driver.press(Key.ENTER);

    driver.waitFor(MainframeConditions.textPresent("Main Menu"));
    driver.screenshot(java.nio.file.Paths.get("after-login.png"));
}
```

## Core concepts

| Concept | Class | Notes |
|---|---|---|
| Driver | `MainframeDriver` | Entry point. `AutoCloseable`. Build with `MainframeDriver.builder()`. |
| Options | `MainframeOptions` | Immutable connection config. `deviceName` is required. |
| Snapshot | `elements.ScreenSnapshot` | Immutable view of text / color / cursor at a moment. |
| Field | `elements.MainframeField` | Wraps `ScreenField`; `.type()`, `.clear()`, `.getText()`. |
| Locator | `locators.By` | `By.at(r,c)`, `By.labelLeftOf("User .:")`, `By.labelAbove(...)`, `By.fieldIndex(n)`, `By.containingText(...)`, `By.firstInputField()`. |
| Key | `keys.Key` | `ENTER`, `PF1`..`PF24`, `CLEAR`, `RESET`, `SYSREQ`, `ATTN`, function/edit keys. |
| Waits | `waits.MainframeConditions` | `inputReady()`, `textPresent(...)`, `cursorAt(r,c)`, `screenStable(Duration)`. |
| Screenshot | `screenshot.ScreenshotRenderer` | Renders snapshot to `BufferedImage` using the canonical 5250 palette. Headless-safe (no display required). |

## Coordinate convention

**All public API uses 1-based row/column** (matches `Screen5250.setCursor` and Personal Communications).
Conversion to/from tn5250j's 0-based field coordinates happens internally.

## Headless vs headed

- `headless(true)` (default) — no UI. Screenshots still work via offscreen rendering.
- `headed()` — opens a `JFrame` that renders the same image and updates on every screen change.

## Why is `deviceName` mandatory?

The AS/400 / IBM i Telnet server closes the session if no `SESSION_DEVICE_NAME` (a.k.a. WorkstationID) is supplied. The builder rejects missing/blank device names eagerly so you fail fast at construction time instead of after the host hangs up.

## Build & run (Gradle)

This module is a self-contained Gradle project. You don't need Gradle installed —
the wrapper downloads it on first run.

```bash
# compile
./gradlew build

# run the example (LoginExample)
./gradlew run

# override host / device / headed window:
./gradlew run -Dhost=MI.HOST -DdeviceName=WS00001 -Dheaded=true
# or positionally:
./gradlew run --args="MI.HOST WS00001"
```

The example connects, prints the sign-on screen, writes `signon.png`, and disconnects.
You can also run/debug `LoginExample` directly from your IDE (import as a Gradle project).

The only declared dependency is the published fork
`com.github.vebqa:tn5250j:0.7.6.4`, which pulls in `log4j`, `slf4j`, and `jt400`
transitively — nothing else to wire up.

## Tests (Cucumber + JUnit 5)

BDD scenarios live in the standard Gradle `src/test` tree and run on the JUnit
Platform:

```
src/test/
  java/com/bns/etbic/craft/mainframe/
    pages/    Page Objects (SignOnPage, MainMenuPage, MainframeScreen)
    steps/    Step definitions
    support/  Per-scenario session lifecycle + config (MainframeSession, Hooks, MainframeConfig)
    runners/  JUnit Platform @Suite that filters the @mainframe tag
  resources/com/bns/etbic/craft/mainframe/features/
    *.feature
```

```bash
# set credentials (read from the environment), then:
export user='MIUSUARIO'
export password='MICLAVE'
./gradlew test

# pick scenarios by tag from the CLI:
./gradlew test -Dcucumber.filter.tags="@mainframe"
```

The mainframe driver is **ephemeral and scenario-scoped**: `MainframeSession`
connects lazily the first time a step calls `session.driver()`, and `Hooks` closes
it after every scenario. So a scenario that is mostly Playwright and only validates
one thing on the mainframe connects just for that step; a scenario that never touches
the mainframe never opens a connection; and an all-mainframe scenario reuses the one
session. The same DI pattern (PicoContainer) lets a Playwright session live alongside
it in the same scenario.

## Moving this module into the craft framework

The package is already `com.bns.etbic.craft.mainframe`, so promoting it is mostly a copy:

1. Copy `src/main/java/com/bns/etbic/craft/mainframe/` into the craft source tree.
2. Drop the `example/` package (it's only the local smoke test).
3. Carry over `src/test/` (pages/steps/support/runners) as the BDD layer.
4. Make sure craft has `com.github.vebqa:tn5250j:0.7.6.4` on its classpath.
