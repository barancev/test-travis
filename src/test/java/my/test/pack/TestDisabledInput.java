package my.test.pack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DashboardNotifications.class)
class TestDisabledInput {

  @Test
  void testDisabledInput() {
    assertThat(2+2).isEqualTo(4);
  }
}
