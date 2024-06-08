package org.paulmanjarres.gradle.timereporter.model;

import java.util.Set;
import lombok.Data;

@Data
public class Histogram {
    private int buckets;
    private int bucketSize;
    private int[] values;
    private double[] percentages;
    private long[] duration;
    private int maxValue;
    private int minValue;
    private int slowTestCount;
    private double slowTestPercentage;
    private long slowTestDuration;
    private int count;
    private long totalTime;

    public static Histogram from(Set<TestExecution> stats, HistogramConfig config) {
        int numberOfBins = config.getMaxValue() / config.getBucketSize();
        int[] histogram = new int[numberOfBins];
        double[] percentages = new double[numberOfBins];
        long[] duration = new long[numberOfBins];
        int slowTestCount = 0;
        long slowTestDuration = 0L;
        int count = stats.size();
        long totalTime = 0L;

        for (TestExecution t : stats) {
            int targetBin = (int) Math.floor(t.getDuration().toMillis() / (double) config.getBucketSize());
            if (targetBin >= numberOfBins) {
                slowTestCount++;
                slowTestDuration += t.getDuration().toMillis();
            } else {
                histogram[targetBin]++;
                duration[targetBin] += t.getDuration().toMillis();
            }
            totalTime += t.getDuration().toMillis();
        }

        for (int i = 0; i < histogram.length; i++) {
            percentages[i] = ((double) histogram[i] * 100 / count);
        }

        final Histogram h = new Histogram();
        h.setBuckets(numberOfBins);
        h.setMaxValue(config.getMaxValue());
        h.setValues(histogram);
        h.setPercentages(percentages);
        h.setDuration(duration);
        h.setSlowTestCount(slowTestCount);
        h.setSlowTestPercentage((double) slowTestCount * 100 / count);
        h.setSlowTestDuration(slowTestDuration);
        h.setCount(stats.size());
        h.setTotalTime(totalTime);
        return h;
    }

    @Data
    public static class HistogramConfig {
        private int maxValue;
        private int bucketSize;
    }
}
