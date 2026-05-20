# `mainframe` — Selenium-style automation façade over tn5250j

A small Java module that wraps [tn5250j](https://github.com/tn5250j/tn5250j) with a
WebDriver-flavoured API for automating AS/400 / IBM i green screens.

## Quick start

```java
import java.time.Duration;
import org.tn5250j.mainframe.MainframeDriver;
import org.tn5250j.mainframe.keys.Key;
import org.tn5250j.mainframe.locators.By;
import org.tn5250j.mainframe.waits.MainframeConditions;

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

## Moving this module to another project

This module is intentionally self-contained:

- No dependencies beyond `tn5250j` and its existing runtime jars.
- The leaf package is **`mainframe`** — the team's automation framework uses the same leaf name.
- The `org.tn5250j.` prefix is a placeholder for development inside this fork. To move:
  1. Copy `src/org/tn5250j/mainframe/` into the target project.
  2. Rename the package root with your IDE (or `sed -i 's/org\.tn5250j\.mainframe/com\.yourframework\.mainframe/g'`).
  3. Ensure `tn5250j` is on the classpath (the existing `lib/runtime/*.jar` set is enough).

## Try it

```bash
# from repo root
mvn -q compile
java -cp out:lib/runtime/* org.tn5250j.mainframe.example.LoginExample
```

(or run `LoginExample` from your IDE — it connects, prints the sign-on screen, writes `signon.png`, and disconnects.)
