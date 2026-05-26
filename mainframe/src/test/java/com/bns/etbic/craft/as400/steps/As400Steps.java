package com.bns.etbic.craft.as400.steps;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bns.etbic.craft.as400.pages.MainMenuPage;
import com.bns.etbic.craft.as400.pages.As400Screen;
import com.bns.etbic.craft.as400.pages.SignOnPage;
import com.bns.etbic.craft.as400.support.As400Config;
import com.bns.etbic.craft.as400.support.As400Session;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public final class As400Steps {

    private final As400Session session;

    private MainMenuPage menu;
    private As400Screen screen;

    // PicoContainer inyecta la misma As400Session del escenario.
    public As400Steps(As400Session session) {
        this.session = session;
    }

    @Given("I am signed on to the AS/400")
    public void i_am_signed_on_to_the_as400() {
        menu = new SignOnPage(session.driver())
            .waitUntilReady()
            .signOn(As400Config.user(), As400Config.password());
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
