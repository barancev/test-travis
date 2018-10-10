import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.safari.SafariDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDisabledInput {

  private WebDriver driver;

  @BeforeEach
  void startDriver() {
    driver = new SafariDriver();
  }

  @Test
  void testDisabledInput() {
    driver.get("http://localhost:8080/test-travis");
    WebElement element = driver.findElement(By.id("disabled"));
    assertThat(element.isEnabled()).isFalse();
  }

  @AfterEach
  void stopDriver() {
    driver.quit();
  }
}
