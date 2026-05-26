package com.bns.etbic.craft.mainframe.runners;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Corre los escenarios Cucumber etiquetados {@code @mainframe}.
 *
 * <pre>./gradlew test</pre>
 * o ejecuta esta clase desde el IDE. El tag se filtra aquí, así que para añadir
 * más escenarios mainframe basta etiquetarlos {@code @mainframe}.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("com/bns/etbic/craft/mainframe/features")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "com.bns.etbic.craft.mainframe.steps,com.bns.etbic.craft.mainframe.support")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@mainframe")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, summary")
public class RunMainframeCucumberTest {
}
