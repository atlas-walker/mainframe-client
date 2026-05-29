package com.bns.etbic.craft.as400;

/**
 * Base class for every 5250 screen Page Object.
 *
 * <p>Subclasses model a single AS/400 screen and expose business-level methods
 * (for example {@code signOn} or {@code selectOption}). They reach the host
 * through the {@code as400} driver shared by {@link As400Factory}, so a page is
 * created with a plain {@code new MyPage()} and never needs the driver injected.
 *
 * <p>The screen queries declared here ({@link #contains(String)} and
 * {@link #text()}) read the live screen, which is what step definitions use to
 * assert the result of an action.
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
public abstract class BasePage {

    /** The shared, connected driver backing this page. */
    protected final As400Driver as400 = As400Factory.getDriver();

    /** Creates a page bound to the shared driver. */
    protected BasePage() {
    }

    /**
     * Tells whether the current host screen contains the given text.
     *
     * @param text the text to look for
     * @return {@code true} if the live screen currently contains {@code text}
     */
    public boolean contains(String text) {
        return as400.getScreen().contains(text);
    }

    /**
     * Returns the full text of the current host screen, mainly for assertion
     * failure messages.
     *
     * @return the current screen rendered as text
     */
    public String text() {
        return as400.screenAsText();
    }
}
