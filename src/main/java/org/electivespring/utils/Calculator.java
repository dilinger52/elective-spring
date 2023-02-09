package org.electivespring.utils;

import java.util.List;
import java.util.Map;

public class Calculator {

    private static Calculator instance;

    public static Calculator getInstance() {
        if (instance == null) {
            instance = new Calculator();
        }
        return instance;
    }

    public int mapSize(Map<?, List<?>> map) {
        return map.values().stream().mapToInt(List::size).sum();
    }
}
