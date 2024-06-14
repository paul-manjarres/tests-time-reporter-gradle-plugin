package org.paulmanjarres.gradle.timereporter.utils;

import lombok.Setter;
import org.gradle.api.tasks.testing.TestResult;

@Setter
public class ConsoleUtils {

    public enum Color {
        BLACK("\033[30m"),
        RED("\033[31m"),
        GREEN("\033[32m"),
        YELLOW("\033[33m"),
        BLUE("\033[34m"),
        MAGENTA("\033[35m"),
        CYAN("\033[36m"),
        CLEAR("\033[0m");

        private final String code;

        Color(String code) {
            this.code = code;
        }
    }

    private boolean colorEnabled;

    public ConsoleUtils(boolean colorEnabled) {
        this.colorEnabled = colorEnabled;
    }

    public String print(String str, Color color) {
        if (!colorEnabled) {
            return str;
        }
        return color.code + str + Color.CLEAR.code;
    }

    public String red(String str) {
        return print(str, Color.RED);
    }

    public String green(String str) {
        return print(str, Color.GREEN);
    }

    public String yellow(String str) {
        return print(str, Color.YELLOW);
    }

    public String magenta(String str) {
        return print(str, Color.MAGENTA);
    }

    public String blue(String str) {
        return print(str, Color.BLUE);
    }

    public String cyan(String str) {
        return print(str, Color.CYAN);
    }

    public ConsoleUtils.Color getColorBy(TestResult.ResultType type) {
        if (type == TestResult.ResultType.FAILURE) {
            return ConsoleUtils.Color.RED;
        } else if (type == TestResult.ResultType.SUCCESS) {
            return ConsoleUtils.Color.GREEN;
        } else if (type == TestResult.ResultType.SKIPPED) {
            return ConsoleUtils.Color.CYAN;
        } else {
            return ConsoleUtils.Color.BLACK;
        }
    }
}
