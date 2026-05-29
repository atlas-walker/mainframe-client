package com.bns.etbic.craft.as400.steps;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bns.etbic.craft.as400.As400Config;
import com.bns.etbic.craft.as400.As400Factory;
import com.bns.etbic.craft.as400.pages.MainMenuPage;
import com.bns.etbic.craft.as400.pages.SignOnPage;

import io.cucumber.java8.En;

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
                () -> "No se encontró la advertencia \"" + expected + "\". Pantalla:\n" + menu.text());
        });
    }
}
