package org.paulmanjarres.gradle.timereporter.utils;

import lombok.Setter;

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

    public String printInRed(String str) {
        if (!colorEnabled) {
            return str;
        }
        return Color.RED.code + str + Color.CLEAR.code;
    }

    public String printInGreen(String str) {
        if (!colorEnabled) {
            return str;
        }
        return Color.GREEN.code + str + Color.CLEAR.code;
    }

    public String printInYellow(String str) {
        if (!colorEnabled) {
            return str;
        }
        return Color.YELLOW.code + str + Color.CLEAR.code;
    }

    public String magenta(String str) {
        if (!colorEnabled) {
            return str;
        }
        return print(str, Color.MAGENTA);
    }
}
