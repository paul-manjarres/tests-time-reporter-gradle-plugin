package org.paulmanjarres.gradle.timereporter;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;

@Data
@AllArgsConstructor
public class TestTimeStats {
  private String testName;
  private Duration duration;
  private String result;


}
