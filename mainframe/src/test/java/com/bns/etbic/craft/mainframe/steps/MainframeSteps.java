package com.bns.etbic.craft.mainframe.steps;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bns.etbic.craft.mainframe.pages.MainMenuPage;
import com.bns.etbic.craft.mainframe.pages.MainframeScreen;
import com.bns.etbic.craft.mainframe.pages.SignOnPage;
import com.bns.etbic.craft.mainframe.support.MainframeConfig;
import com.bns.etbic.craft.mainframe.support.MainframeSession;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public final class MainframeSteps {

    private final MainframeSession session;

    private MainMenuPage menu;
    private MainframeScreen screen;

    // PicoContainer inyecta la misma MainframeSession del escenario.
    public MainframeSteps(MainframeSession session) {
        this.session = session;
    }

    @Given("I am signed on to the mainframe")
    public void i_am_signed_on_to_the_mainframe() {
        menu = new SignOnPage(session.driver())
            .waitUntilReady()
            .signOn(MainframeConfig.user(), MainframeConfig.password());
    }

    @When("I select menu option {string}")
    public void i_select_menu_option(String option) {
        screen = menu.selectOption(option);
    }

    @Then("the mainframe warns {string}")
    public void the_mainframe_warns(String expected) {
        assertTrue(
            screen.contains(expected),
            () -> "No se encontró la advertencia \"" + expected + "\". Pantalla:\n" + screen.text());
    }
}
