package com.eNeNzik.vip;

import org.bukkit.ChatColor;

import java.util.Locale;
import java.util.Map;

public final class ColorTokens {

    private ColorTokens() {
    }

    private static final Map<String, String> NAMED = Map.ofEntries(
            Map.entry("black", ChatColor.BLACK.toString()),
            Map.entry("dark_blue", ChatColor.DARK_BLUE.toString()),
            Map.entry("dark_green", ChatColor.DARK_GREEN.toString()),
            Map.entry("dark_aqua", ChatColor.DARK_AQUA.toString()),
            Map.entry("dark_red", ChatColor.DARK_RED.toString()),
            Map.entry("dark_purple", ChatColor.DARK_PURPLE.toString()),
            Map.entry("gold", ChatColor.GOLD.toString()),
            Map.entry("gray", ChatColor.GRAY.toString()),
            Map.entry("grey", ChatColor.GRAY.toString()),
            Map.entry("dark_gray", ChatColor.DARK_GRAY.toString()),
            Map.entry("dark_grey", ChatColor.DARK_GRAY.toString()),
            Map.entry("blue", ChatColor.BLUE.toString()),
            Map.entry("green", ChatColor.GREEN.toString()),
            Map.entry("aqua", ChatColor.AQUA.toString()),
            Map.entry("red", ChatColor.RED.toString()),
            Map.entry("light_purple", ChatColor.LIGHT_PURPLE.toString()),
            Map.entry("yellow", ChatColor.YELLOW.toString()),
            Map.entry("white", ChatColor.WHITE.toString()),

            Map.entry("bold", ChatColor.BOLD.toString()),
            Map.entry("italic", ChatColor.ITALIC.toString()),
            Map.entry("underline", ChatColor.UNDERLINE.toString()),
            Map.entry("underlined", ChatColor.UNDERLINE.toString()),
            Map.entry("strikethrough", ChatColor.STRIKETHROUGH.toString()),
            Map.entry("magic", ChatColor.MAGIC.toString()),
            Map.entry("obfuscated", ChatColor.MAGIC.toString()),
            Map.entry("reset", ChatColor.RESET.toString())
    );

    /**
     * Translates color tags like "#red#", "#dark_green#", or "#ff00ff#".
     * Tags can be written inline, for example: "#blue#e#red#N#green#e#yellow#N#aqua#zik".
     */
    public static String translateHashColors(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder out = new StringBuilder(input.length() + 16);

        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);

            if (current == '#') {
                int end = input.indexOf('#', i + 1);
                if (end != -1) {
                    String token = input.substring(i + 1, end);
                    String code = codeFor(token);
                    if (code != null) {
                        out.append(code);
                        i = end;
                        continue;
                    }
                }
            }

            out.append(current);
        }

        return out.toString();
    }

    private static String codeFor(String token) {
        String key = token.toLowerCase(Locale.ROOT);
        String named = NAMED.get(key);
        if (named != null) {
            return named;
        }

        if (key.length() == 6 && key.matches("[0-9a-f]{6}")) {
            try {
                return net.md_5.bungee.api.ChatColor.of("#" + key).toString();
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return null;
    }
}

