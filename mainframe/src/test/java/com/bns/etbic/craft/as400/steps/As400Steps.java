package com.bns.etbic.craft.as400.steps;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bns.etbic.craft.as400.As400Config;
import com.bns.etbic.craft.as400.As400Factory;
import com.bns.etbic.craft.as400.pages.MainMenuPage;
import com.bns.etbic.craft.as400.pages.As400Screen;
import com.bns.etbic.craft.as400.pages.SignOnPage;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public final class As400Steps {

    private MainMenuPage menu;
    private As400Screen screen;

    @Given("I am signed on to the AS/400")
    public void i_am_signed_on_to_the_as400() {
        As400Config cfg = As400Factory.currentConfig();
        menu = new SignOnPage()
            .waitUntilReady()
            .signOn(cfg.user(), cfg.password());
    }

    @When("I select menu option {string}")
    public void i_select_menu_option(String option) {
        screen = menu.selectOption(option);
    }

    @Then("the AS/400 warns {string}")
    public void the_as400_warns(String expected) {
        assertTrue(
            screen.contains(expected),
            () -> "No se encontró la advertencia \"" + expected + "\". Pantalla:\n" + screen.text());
    }
}
