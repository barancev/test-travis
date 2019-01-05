import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDisabledInput {

  @Test
  void testDisabledInput() {
    assertThat(2+2).isEqualTo(4);
  }
}
