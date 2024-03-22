package org.paulmanjarres.gradle.timereporter;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestTimeStats {
  private String testName;
  private Duration duration;
  private String result;
}
