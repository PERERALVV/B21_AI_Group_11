package qatraining.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

/**
 * Runs only the Authentication module features (Janith's part) in isolation:
 *   mvn clean verify -Dit.test=AuthTestSuite
 */
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        plugin = {"pretty"},
        features = "src/test/resources/features/auth",
        glue = "qatraining.steps"
)
public class AuthTestSuite {}
