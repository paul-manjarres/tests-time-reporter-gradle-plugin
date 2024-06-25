package org.paulmanjarres.gradle.timereporter.model;

public class PluginExtensionDefaultValues {

    private PluginExtensionDefaultValues() {}

    public static final int longestTestsCount = 10;
    public static final int binSizeInMillis = 100;
    public static final int slowThresholdInMillis = 200;
    public static final int maxResultsForGroupByClass = 5;
    public static final int MAX_RESULTS_FOR_TREE_VIEW_SUITES = 15;
    public static final int MIN_TESTS_FOR_EXECUTION = 3;
    public static final boolean showGroupByResult = true;
    public static final boolean showGroupByClass = true;
    public static final boolean showSlowestTests = true;
    public static final boolean showHistogram = true;
    public static final boolean coloredOutput = true;
    public static final boolean experimentalFeatures = false;
    public static final boolean ENABLED = true;
    public static final boolean SHOW_SKIPPED = true;
    public static final boolean SHOW_FAILED = true;
    public static final boolean SHOW_TREE_VIEW = true;
}
