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
2. Run `git diff master...HEAD --name-only` to list modified files
3. Run `git diff master...HEAD` to get the full diff
4. Run `git log master..HEAD --oneline` to get the commit history
5. Analyze every modified file against ALL convention sections in this document
6. Generate the report as a `.md` file in the project root

If the user specifies a different branch, use it instead of HEAD.
If the user specifies a different base branch, use it instead of master.

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

The report must be generated as a Markdown file in the project root with the name:
`review-{branch-name}.md`

For example, for the branch `feature/login-automation`: `review-feature-login-automation.md`

### Report Structure

```markdown
# Code Review: {branch name}

**Date:** {current date}
**Files reviewed:** {count}
**Commits:** {count}

---

## ✅ What's Done Well

(Mention at least 3 positive aspects of the code. Always lead with the good stuff.)

---

## 🚫 Blocking Findings

(For EACH finding, use this structure:)

### [B-001] Descriptive title of the finding
- **File:** `path/to/file.java` (line ~N)
- **What was found:** Clear description of the issue
- **Why it matters:** Explain the impact or risk
- **How to improve:**
  (Include a corrected code example when applicable)

---

## ⚠️ Warnings

### [W-001] Descriptive title
- **File:** `path/to/file.java` (line ~N)
- **What was found:** Description
- **Suggestion:** How to improve it

---

## 📝 Commit Review

(Evaluate each commit against the conventions)

---

## 📊 Summary

| Category   | Count |
|----------- |-------|
| Blocking   | X     |
| Warnings   | X     |

**Verdict:** Approve / Request Changes / Comment
```

### Report Rules

- **Approve:** Zero blocking findings
- **Request Changes:** At least one blocking finding
- **Comment:** Only warnings, but relevant enough to discuss
- Every finding must include: what's wrong, why it matters, and how to improve it
- Tone must be constructive: "What about...?" / "You might consider..." instead of "This is wrong"
- Always start with what's done well before listing findings
