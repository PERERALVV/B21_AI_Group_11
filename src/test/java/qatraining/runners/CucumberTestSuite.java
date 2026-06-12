package qatraining.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

/**
 * Single test runner for the whole project. Discovers every module's feature
 * files under src/test/resources/features (sales, auth, and any added later).
 */
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        plugin = {"pretty"},
        features = "src/test/resources/features",
        glue = "qatraining.steps"
)
public class CucumberTestSuite {}
