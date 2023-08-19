package com.kepchyk1101.AirAlertsTGBot.utils;

import java.util.Map;

public class TextFormatter {

    // Замена плейсходеров в строках по типу {regionName}, {time} и т.д.
    public static String format(String str, Map<String, String> placeholders) {

        for (String placeholder : placeholders.keySet())
            if (str.contains(placeholder))
                str = str.replace(placeholder, placeholders.get(placeholder));

        return str;

    }

}