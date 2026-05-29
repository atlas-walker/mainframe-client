package com.bns.etbic.craft.as400;

/**
 * Base de todos los Page Object de pantallas 5250. Expone el driver compartido
 * por {@link As400Factory}; las subclases lo usan vía el campo {@code as400}.
 */
public abstract class BasePage {

    protected final As400Driver as400 = As400Factory.getDriver();
}
