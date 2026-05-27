---
name: pr-review-automation
description: >
  Reviews Pull Request changes applying the automation team's conventions.
  Use this skill when the user asks to review a PR, do a code review,
  review branch changes, analyze a diff, or evaluate test automation code.
  Stack: Java 21+, JUnit 5, Cucumber, Gradle, Page Object Model, Craft framework.
tools:
  - shell
---

# PR Review — Automation Team Conventions

## Review Workflow

When asked to review a PR or branch changes:

1. Run `git rev-parse --abbrev-ref HEAD` to get the current branch name
2. Run `git --no-pager diff master...HEAD --name-only` to list modified files
3. Run `git --no-pager diff master...HEAD` to get the full diff
4. Run `git --no-pager log master..HEAD --oneline` to get the commit history
5. Analyze every modified file against ALL convention sections in this document
6. Generate the report as a `.md` file in the project root

If the user specifies a different branch, use it instead of HEAD.
If the user specifies a different base branch, use it instead of master.

**CRITICAL:** ALL git commands MUST use `--no-pager` to prevent the terminal from opening an interactive pager (like `less` or `vim`) that blocks execution. Never run a git command without `--no-pager` when reading output.

**CRITICAL:** This skill ONLY generates a report. It MUST NEVER modify files in `src/` or any other project directory. The only output is a `.md` file with the findings.

---

## 1. Stack & Modern Syntax (Java 21+)

### 🚫 Blocking

- Using legacy syntax when a modern Java 21+ alternative exists:
  - `instanceof` without pattern matching → must use `if (obj instanceof String s)`
  - Chained `if-else` over types → must use switch with pattern matching
  - Multiline string concatenation → must use text blocks (`"""`)
  - Classic switch blocks where applicable → must use switch expressions with `->`
  - Data-only helper classes without behavior → must use records
  - `var` where the type is NOT obvious from the right-hand side
  - Mutable collections where mutability is not needed → must use `List.of()`, `Map.of()`, `Set.of()`

### ⚠️ Warnings

- Opportunity to use `Optional` instead of manual null checks
- Opportunity to use streams where there are simple transformation or filtering loops
- Usage of APIs deprecated in Java 21+

---

## 2. Feature Files (Cucumber / Gherkin)

### 🚫 Blocking

- Usage of `Background`. The Craft framework runs each case in full isolation; there must be NO dependency between scenarios. Background violates this principle
- Scenario WITHOUT the mandatory first step `Given I successfully load the case data "CASE-ID-XXX"`. This step loads the case data and MUST always be the first one
- Steps that reference the UI directly. The team follows the **Business Actions** approach:
  - ❌ `When I click the login button`
  - ❌ `When I type "user@mail.com" in the email field`
  - ❌ `When I select the dropdown option "Admin"`
  - ✅ `When I log in with valid credentials`
  - ✅ `When I create a new purchase order`
  - ✅ `When I approve the pending request`
- Dependency between scenarios (a scenario assumes another one has already run)

### ⚠️ Warnings

- Scenario with more than 10 steps (consider grouping actions into higher-level steps)
- Steps with hardcoded data that should come from the case data
- Vague scenario name that does not describe the test case
- Missing tags for categorization (`@smoke`, `@regression`, etc.)
- Scenario Outline without enough Examples to cover relevant variations

### Expected Good Practices

- Each scenario must be self-contained and independently executable
- Steps should read as business actions understandable by non-technical stakeholders
- `Then` steps should validate business outcomes, not UI state

---

## 3. Package Structure

### 🚫 Blocking

- File placed outside the three mandatory packages
- Validation logic or interaction code inside `step_definitions` (must only invoke `page_objects` methods)
- Execution logic inside `runner`

### Mandatory Structure

```
src/
├── runner/              → Execution configuration (Cucumber runner, Craft hooks)
├── page_objects/        → All logic: locators, actions, validations, try-catch
└── step_definitions/    → ONLY invocation of page_objects methods, nothing else
```

### Critical Rule for `step_definitions`

Step definitions are a bridge between Cucumber and page objects. They must ONLY contain calls to page object methods. If any method that can throw an exception is placed here, the Craft framework will NOT be aware of the failure because exceptions are not captured in this layer.

```java
// ✅ CORRECT — Only invocation
@When("I log in with valid credentials")
public void iLogInWithValidCredentials() {
    loginPage.login();
}

// ❌ INCORRECT — Logic that can fail outside page objects
@When("I log in with valid credentials")
public void iLogInWithValidCredentials() {
    driver.findElement(By.id("user")).sendKeys(data.get("user"));
    driver.findElement(By.id("pass")).sendKeys(data.get("pass"));
    driver.findElement(By.id("loginBtn")).click();
}
```

---

## 4. Page Objects & Exception Handling

### 🚫 Blocking

#### 4.1 Main method without try-catch

Every main method (the one invoked from step definitions) MUST have the mandatory try-catch block:

```java
// ✅ CORRECT
public void login() {
    try {
        enterUsername();
        enterPassword();
        clickLoginButton();
        reporter.casePassed("Login executed successfully");
    } catch (Exception | AssertionError e) {
        reporter.caseFailed("Login failed for the user", e);
    }
}
```

```java
// ❌ INCORRECT — No try-catch
public void login() {
    enterUsername();
    enterPassword();
    clickLoginButton();
}
```

#### 4.2 `reporter.caseFailed()` in helper methods

`reporter.caseFailed()` MUST ONLY be called in the main method. If a helper method also calls it, it generates noise in the reporter and logger, making it impossible to determine where the failure actually occurred.

```java
// ❌ INCORRECT — caseFailed in helper method
private void enterUsername() {
    try {
        // logic
    } catch (Exception | AssertionError e) {
        reporter.caseFailed("Failed to enter username", e);  // ← NOT ALLOWED HERE
    }
}

// ✅ CORRECT — Helper uses addErrorLog and re-throws
private void enterUsername() {
    try {
        // logic
    } catch (Exception e) {
        Reporter.addErrorLog("Error entering username", e.getMessage());
        throw e;  // re-throw so the main method catches it and calls caseFailed
    }
}
```

#### 4.3 Reporter redundancy

Do NOT use `reporter.addReportEvent("message", ReporterStatus.INFO)` together with `reporter.casePassed()` at the end of the same method. Both generate a screenshot and an entry in the Extent Report, causing duplicates. Use only one of them.

```java
// ❌ INCORRECT — Redundancy
public void login() {
    try {
        enterUsername();
        enterPassword();
        clickLoginButton();
        reporter.addReportEvent("Login successful", ReporterStatus.INFO);  // screenshot + entry
        reporter.casePassed("Login executed successfully");                // another screenshot + entry
    } catch (Exception | AssertionError e) {
        reporter.caseFailed("Login failed for the user", e);
    }
}

// ✅ CORRECT — Only one
public void login() {
    try {
        enterUsername();
        enterPassword();
        clickLoginButton();
        reporter.casePassed("Login executed successfully");
    } catch (Exception | AssertionError e) {
        reporter.caseFailed("Login failed for the user", e);
    }
}
```

---

## 5. Page Object Model (POM) & Locators

### 🚫 Blocking

- Fragile locators:
  - ❌ Index-based (`//div[3]/span[2]`)
  - ❌ Absolute XPaths (`/html/body/div/form/input`)
  - ❌ Position-dependent locators that break when the DOM changes
- Locators or interactions that do not follow the POM pattern (direct driver actions in step definitions)
- Duplicated locators across page objects

### ⚠️ Warnings

- Locators with dynamically generated CSS classes (e.g., `class="btn-a1b2c3"`)
- Page object managing more than one page or logical section
- Lack of encapsulation: locator exposed as public

### Recommended Locators (most to least resilient)

1. `By.id("loginButton")` — most stable
2. `By.name("username")` — stable if names are consistent
3. `By.cssSelector("[data-testid='login-btn']")` — with testing attributes
4. `By.cssSelector(".login-form .submit-btn")` — semantic classes
5. `By.xpath("//button[contains(text(),'Log in')]")` — text-based, last resort

---

## 6. Code Quality & Clean Code

### 🚫 Blocking

- Hardcoded credentials, tokens, or secrets
- Code that clearly violates SOLID principles:
  - Class with multiple unrelated responsibilities
  - Direct dependency on concrete implementations where abstraction is expected
- **Method name does not match its actual behavior.** If a method is named `verify`, `validate`, `check`, `assert`, or similar, it MUST actually perform a verification or assertion. If the method only navigates, clicks, or performs actions without any assertion, it must be renamed to reflect what it truly does. This is a blocking finding because misleading method names erode trust in the test suite and hide missing validations.

  ```java
  // ❌ INCORRECT — Named "verify" but verifies nothing
  public void verifyHomePage() {
      clickMenuButton();
      navigateToProfile();
      scrollToBottom();
  }

  // ✅ CORRECT — Name matches behavior (option A: rename)
  public void navigateHomePageSections() {
      clickMenuButton();
      navigateToProfile();
      scrollToBottom();
  }

  // ✅ CORRECT — Name matches behavior (option B: add actual verification)
  public void verifyHomePage() {
      assertTrue(homeTitle.isDisplayed());
      assertTrue(menuSection.isDisplayed());
      assertEquals("Welcome", homeTitle.getText());
      reporter.casePassed("Home page elements verified successfully");
  }
  ```

  Common mismatches to look for:
  - `verify*` / `validate*` / `check*` / `assert*` → must contain at least one assertion
  - `click*` / `navigate*` / `open*` → must NOT contain assertions unrelated to navigation
  - `get*` → must return a value; flag if it returns void
  - `set*` → must assign or configure something; flag if it only reads

### ⚠️ Warnings

- Method longer than 30 lines → recommend refactoring into smaller, readable helper methods
- Class longer than 300 lines
- Names that do not follow conventions:
  - Constants: `SNAKE_CASE` → `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`
  - Variables and methods: `camelCase`
  - Classes: `PascalCase`
- Commented-out code without justification (remove or create a ticket)
- `TODO` / `FIXME` without an associated ticket
- Missing documentation on public page object methods
- Magic numbers without a constant (e.g., `Thread.sleep(5000)` → `Thread.sleep(DEFAULT_WAIT)`)
- Helper methods that should be extracted to a shared utility class

---

## 7. Commit Conventions

### 🚫 Blocking

- Commit without a conventional type prefix
- Commit that mixes more than one responsibility

### Mandatory Format

```
type: description in Spanish
```

Valid types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `style`, `perf`

### ⚠️ Warnings

- Vague description (e.g., "changes", "fix", "update", "wip")
- Description in English (the team writes commit messages in Spanish)
- Oversized commit (more than 10 files changed in a single commit)

---

## Report Format

The report must be generated as an **HTML file** in the `report/pr/` directory with the name:
`review-{branch-name}.html`

For example, for the branch `feature/login-automation`: `report/pr/review-feature-login-automation.html`

If the `report/pr/` directory does not exist, create it before writing the file:
```bash
mkdir -p report/pr
```

After generating the report, **always open it automatically** in the default browser:
```bash
open report/pr/review-{branch-name}.html
```
On Linux use `xdg-open` instead of `open`.

### Report Rules

- **Approve:** Zero blocking findings
- **Request Changes:** At least one blocking finding
- **Comment:** Only warnings, but relevant enough to discuss
- Every finding must include: what's wrong, why it matters, and how to improve it
- Tone must be constructive: "What about...?" / "You might consider..." instead of "This is wrong"
- Always start with what's done well before listing findings
- **NO emojis anywhere in the report.** The report must look professional. Use the CSS badges and color coding for visual distinction instead of emoji characters. This applies to all text: titles, descriptions, suggestions, commit review, and the verdict banner

### HTML Report Template

Generate the report following this HTML structure. Replace all `{placeholders}` with actual data.
Apply these color rules for the verdict banner:
- **Approve** → green (`#16a34a`)
- **Request Changes** → red (`#dc2626`)
- **Comment** → yellow/amber (`#d97706`)

Each finding row in the table must have a severity badge:
- 🚫 Blocking → red badge
- ⚠️ Warning → amber badge

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Code Review: {branch-name}</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f8fafc; color: #1e293b; padding: 2rem; line-height: 1.6; }
    .container { max-width: 960px; margin: 0 auto; }
    h1 { font-size: 1.5rem; margin-bottom: 0.25rem; }
    .meta { color: #64748b; font-size: 0.875rem; margin-bottom: 1.5rem; }

    /* Verdict banner */
    .verdict { padding: 1rem 1.5rem; border-radius: 8px; color: #fff; font-weight: 700; font-size: 1.125rem; margin-bottom: 2rem; display: flex; justify-content: space-between; align-items: center; }
    .verdict-approve  { background: #16a34a; }
    .verdict-changes  { background: #dc2626; }
    .verdict-comment  { background: #d97706; }
    .verdict .stats span { font-weight: 400; font-size: 0.875rem; margin-left: 1.5rem; }

    /* Sections */
    .section { background: #fff; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 1.5rem; overflow: hidden; }
    .section-header { padding: 0.75rem 1.25rem; font-weight: 600; font-size: 1rem; border-bottom: 1px solid #e2e8f0; }
    .section-header.good { background: #f0fdf4; color: #166534; }
    .section-header.blocking { background: #fef2f2; color: #991b1b; }
    .section-header.warning { background: #fffbeb; color: #92400e; }
    .section-header.commits { background: #eff6ff; color: #1e40af; }

    /* Good things list */
    .good-list { padding: 1rem 1.25rem; }
    .good-list li { margin-bottom: 0.5rem; list-style: none; }
    .good-list li::before { content: ""; }

    /* Findings table */
    table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    th { text-align: left; padding: 0.625rem 1rem; background: #f8fafc; border-bottom: 2px solid #e2e8f0; color: #64748b; font-weight: 600; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; }
    td { padding: 0.75rem 1rem; border-bottom: 1px solid #f1f5f9; vertical-align: top; }
    tr:last-child td { border-bottom: none; }
    tr:hover { background: #f8fafc; }

    /* Severity badges */
    .badge { display: inline-block; padding: 0.125rem 0.625rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; }
    .badge-blocking { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
    .badge-warning  { background: #fffbeb; color: #d97706; border: 1px solid #fde68a; }

    /* Code snippets */
    code { background: #f1f5f9; padding: 0.125rem 0.375rem; border-radius: 4px; font-size: 0.8125rem; font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace; }
    pre { background: #1e293b; color: #e2e8f0; padding: 1rem; border-radius: 6px; overflow-x: auto; margin: 0.5rem 0; font-size: 0.8125rem; }

    /* Commit review */
    .commit-list { padding: 1rem 1.25rem; }
    .commit-item { padding: 0.5rem 0; border-bottom: 1px solid #f1f5f9; }
    .commit-item:last-child { border-bottom: none; }
    .commit-hash { font-family: monospace; color: #6366f1; font-size: 0.8125rem; }
    .commit-msg { margin-left: 0.5rem; }
    .commit-issue { color: #dc2626; font-size: 0.8125rem; margin-top: 0.25rem; padding-left: 1rem; }
    .commit-ok { color: #16a34a; font-size: 0.8125rem; margin-top: 0.25rem; padding-left: 1rem; }
  </style>
</head>
<body>
  <div class="container">
    <h1>Code Review: {branch-name}</h1>
    <p class="meta">Date: {date} &bull; Files reviewed: {file-count} &bull; Commits: {commit-count}</p>

    <!-- VERDICT BANNER: use verdict-approve, verdict-changes, or verdict-comment -->
    <div class="verdict {verdict-class}">
      <span>{verdict-text}</span>
      <div class="stats">
        <span>{blocking-count} Blocking</span>
        <span>{warning-count} Warnings</span>
      </div>
    </div>

    <!-- WHAT'S DONE WELL -->
    <div class="section">
      <div class="section-header good">What's Done Well</div>
      <ul class="good-list">
        <li>{positive finding 1}</li>
        <li>{positive finding 2}</li>
        <li>{positive finding 3}</li>
      </ul>
    </div>

    <!-- BLOCKING FINDINGS (omit section if zero) -->
    <div class="section">
      <div class="section-header blocking">Blocking Findings</div>
      <table>
        <thead>
          <tr>
            <th style="width:60px">ID</th>
            <th style="width:90px">Severity</th>
            <th>Finding</th>
            <th style="width:220px">File</th>
          </tr>
        </thead>
        <tbody>
          <!-- Repeat this <tr> for each blocking finding -->
          <tr>
            <td>B-001</td>
            <td><span class="badge badge-blocking">Blocking</span></td>
            <td>
              <strong>{title}</strong><br>
              <strong>What was found:</strong> {description}<br>
              <strong>Why it matters:</strong> {impact}<br>
              <strong>How to improve:</strong><br>
              <pre>{corrected code example}</pre>
            </td>
            <td><code>{file:line}</code></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- WARNINGS (omit section if zero) -->
    <div class="section">
      <div class="section-header warning">Warnings</div>
      <table>
        <thead>
          <tr>
            <th style="width:60px">ID</th>
            <th style="width:90px">Severity</th>
            <th>Finding</th>
            <th style="width:220px">File</th>
          </tr>
        </thead>
        <tbody>
          <!-- Repeat this <tr> for each warning -->
          <tr>
            <td>W-001</td>
            <td><span class="badge badge-warning">Warning</span></td>
            <td>
              <strong>{title}</strong><br>
              <strong>What was found:</strong> {description}<br>
              <strong>Suggestion:</strong> {how to improve}
            </td>
            <td><code>{file:line}</code></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- COMMIT REVIEW -->
    <div class="section">
      <div class="section-header commits">Commit Review</div>
      <div class="commit-list">
        <!-- Repeat for each commit -->
        <div class="commit-item">
          <span class="commit-hash">{short-hash}</span>
          <span class="commit-msg">{commit message}</span>
          <!-- Use commit-issue for problems, commit-ok if compliant -->
          <div class="commit-issue">{issue description, if any}</div>
        </div>
      </div>
    </div>

  </div>
</body>
</html>
```
