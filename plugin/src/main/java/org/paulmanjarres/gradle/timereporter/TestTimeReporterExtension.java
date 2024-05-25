package org.paulmanjarres.gradle.timereporter;

import org.gradle.api.provider.Property;

/**
 * The Plugin extension class.
 * Registers the configuration options.
 * @author <a href="mailto:paul.manjarres@gmail.com">Jean Paul Manjarres Correal</a>
 */
public abstract class TestTimeReporterExtension {

    /**
     * The max amount of tests to show.
     * @return Property of type Integer
     */
    public abstract Property<Integer> getLongestTestsCount();

    /**
     * The max amount of results for the grouping of tests by class.
     * @return Property of type Integer
     */
    public abstract Property<Integer> getMaxResultsForGroupByClass();

    /**
     * The size of the partitions for the histogram.
     * @return Property of type Integer
     */
    public abstract Property<Integer> getBinSizeInMillis();

    /**
     * The threshold to consider a test as slow, in milliseconds.
     * @return Property of type Integer
     */
    public abstract Property<Integer> getSlowThresholdInMillis();

    public abstract Property<Boolean> getShowGroupByResult();

    public abstract Property<Boolean> getShowGroupByClass();

    public abstract Property<Boolean> getShowSlowestTests();

    public abstract Property<Boolean> getShowHistogram();

    public abstract Property<Boolean> getColoredOutput();

    public abstract Property<Boolean> getExperimentalFeatures();
}
