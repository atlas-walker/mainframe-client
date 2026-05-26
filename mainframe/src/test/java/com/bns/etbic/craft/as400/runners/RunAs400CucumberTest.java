package com.bns.etbic.craft.as400.runners;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Corre los escenarios Cucumber etiquetados {@code @as400}.
 *
 * <pre>./gradlew test</pre>
 * o ejecuta esta clase desde el IDE. El tag se filtra aquí, así que para añadir
 * más escenarios as400 basta etiquetarlos {@code @as400}.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("com/bns/etbic/craft/as400/features")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "com.bns.etbic.craft.as400.steps,com.bns.etbic.craft.as400.support")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@as400")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, summary")
public class RunAs400CucumberTest {
}
