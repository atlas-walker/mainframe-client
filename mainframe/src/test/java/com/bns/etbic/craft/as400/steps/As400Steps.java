package com.bns.etbic.craft.as400.steps;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bns.etbic.craft.as400.As400Config;
import com.bns.etbic.craft.as400.As400Factory;
import com.bns.etbic.craft.as400.pages.MainMenuPage;
import com.bns.etbic.craft.as400.pages.SignOnPage;

import io.cucumber.java8.En;

/**
 * Cucumber step definitions for the AS/400 scenarios, written in the Java 8 lambda
 * style ({@link io.cucumber.java8.En}).
 *
 * <p>Each step builds the Page Object it needs and drives it; assertions read the
 * live screen through the page's query methods.
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public class As400Steps implements En {

    public As400Steps() {

        Given("I am signed on to the AS/400", () -> {
            As400Config cfg = As400Factory.currentConfig();
            new SignOnPage().signOn(cfg.user(), cfg.password());
        });

        When("I select menu option {string}", (String option) -> {
            new MainMenuPage().selectOption(option);
        });

        Then("the AS/400 warns {string}", (String expected) -> {
            MainMenuPage menu = new MainMenuPage();
            assertTrue(
                menu.contains(expected),
                () -> "Warning \"" + expected + "\" not found. Screen:\n" + menu.text());
        });
    }
}
