package org.paulmanjarres.gradle.timereporter.model;

import java.util.Set;
import lombok.Data;

@Data
public class Histogram {
    private int buckets;
    private int bucketSize;
    private int[] values;
    private double[] percentages;
    private int maxValue;
    private int minValue;
    private int slowTestCount;
    private double slowTestPercentage;
    private int count;

    public static Histogram from(Set<TestTimeExecutionStats> stats, HistogramConfig config) {
        int numberOfBins = config.getMaxValue() / config.getBucketSize();
        int[] histogram = new int[numberOfBins];
        double[] percentages = new double[numberOfBins];
        int slowTestCount = 0;
        int count = stats.size();

        for (TestTimeExecutionStats t : stats) {
            int targetBin = (int) Math.floor(t.getDuration().toMillis() / (double) config.getBucketSize());
            if (targetBin >= numberOfBins) {
                slowTestCount++;
            } else {
                histogram[targetBin]++;
            }
        }

        for (int i = 0; i < histogram.length; i++) {
            percentages[i] = ((double) histogram[i] * 100 / count);
        }

        final Histogram h = new Histogram();
        h.setBuckets(numberOfBins);
        h.setMaxValue(config.getMaxValue());
        h.setValues(histogram);
        h.setPercentages(percentages);
        h.setSlowTestCount(slowTestCount);
        h.setSlowTestPercentage((double) slowTestCount * 100 / count);
        h.setCount(stats.size());
        return h;
    }

    @Data
    public static class HistogramConfig {
        private int maxValue;
        private int bucketSize;
    }
}
