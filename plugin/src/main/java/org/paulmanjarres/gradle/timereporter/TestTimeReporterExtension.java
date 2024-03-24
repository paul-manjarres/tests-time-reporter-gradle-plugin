package org.paulmanjarres.gradle.timereporter;

import org.gradle.api.provider.Property;

public abstract class TestTimeReporterExtension {

    /**
     * The max amount of tests to show.
     * @return Property of type Integer
     */
    public abstract Property<Integer> getLongestTestsCount();

    public abstract Property<Integer> getBinSizeInMillis();

    /**
     * The threshold to consider a test as slow, in milliseconds.
     * @return Property of type Integer
     */
    public abstract Property<Integer> getSlowThresholdInMillis();
}
