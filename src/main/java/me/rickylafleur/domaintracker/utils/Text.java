package me.rickylafleur.domaintracker.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Text {

    private static final Pattern HEX_PATTERN = Pattern.compile("&(#\\w{6})");

    private Text() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static String colorize(String string) {
        var matcher = HEX_PATTERN.matcher(ChatColor.translateAlternateColorCodes('&', string));
        var builder = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(builder, ChatColor.of(matcher.group(1)).toString());
        }

        return matcher.appendTail(builder).toString();
    }

    public static List<String> colorize(List<String> strings) {
        var toReturn = new ArrayList<String>();

        for (String string : strings) {
            toReturn.add(colorize(string));
        }

        return toReturn;
    }

    public static String[] colorize(String[] strings) {
        var toReturn = new String[strings.length];

        for (int i = 0; i < strings.length; i++) {
            toReturn[i] = colorize(strings[i]);
        }

        return toReturn;
    }

}