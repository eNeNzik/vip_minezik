package com.eNeNzik.vip;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VipTabCompleter implements TabCompleter {

    private static final String[] PETS_UA = {
            "елей", "кішка", "собака", "папуга", "курка", "корова", "свиня", "вівця", "кінь", "осел", "лама",
            "кролик", "лисиця", "панда", "бджола", "коза", "жаба", "верблюд", "броненосець", "черепаха",
            "черепашка", "зомбі", "зомбі_дитинча", "скелет", "кріпер", "павук", "ендермен", "відьма",
            "слизень", "фантом", "хоглін", "піглін", "зомбі_піглін", "лосось", "дитинча_лосося",
            "великий_лосось", "рибка", "тріска", "іглобрюх", "аксолотль", "дельфін", "кальмар",
            "світний_кальмар", "селянин", "бідний_селянин", "залізний_голем", "сніговик"
    };

    private static final String[] PETS_EN = {
            "allay", "cat", "dog", "wolf", "parrot", "chicken", "cow", "pig", "sheep", "horse", "donkey",
            "llama", "rabbit", "fox", "panda", "bee", "goat", "frog", "camel", "armadillo", "turtle",
            "baby_turtle", "zombie", "baby_zombie", "skeleton", "creeper", "spider", "enderman", "witch",
            "slime", "phantom", "hoglin", "piglin", "zombified_piglin", "salmon", "small_salmon",
            "large_salmon", "tropical_fish", "cod", "pufferfish", "axolotl", "dolphin", "squid",
            "glow_squid", "villager", "baby_villager", "iron_golem", "snow_golem"
    };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            addIfMatches(completions, args[0], "help");
            addIfMatches(completions, args[0], "language");
            addIfMatches(completions, args[0], "lang");
            addIfMatches(completions, args[0], "particles");
            addIfMatches(completions, args[0], "join_message");
            addIfMatches(completions, args[0], "leave_message");
            addIfMatches(completions, args[0], "ender_chest");
            addIfMatches(completions, args[0], "craft");
            addIfMatches(completions, args[0], "nick_color");
            addIfMatches(completions, args[0], "chatcolor");
            addIfMatches(completions, args[0], "prefix");
            addIfMatches(completions, args[0], "suffix");
            addIfMatches(completions, args[0], "pet");
            if (hasVipPlus(sender)) {
                addIfMatches(completions, args[0], "kit");
                addIfMatches(completions, args[0], "rename");
                addIfMatches(completions, args[0], "addFriend");
                addIfMatches(completions, args[0], "announcement");
            }
            return completions;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("language") || args[0].equalsIgnoreCase("lang"))) {
            addIfMatches(completions, args[1], "auto");
            addIfMatches(completions, args[1], "ua");
            addIfMatches(completions, args[1], "en");
            addIfMatches(completions, args[1], "ru");
            addIfMatches(completions, args[1], "pl");
            addIfMatches(completions, args[1], "de");
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("addfriend")) {
            if (!hasVipPlus(sender)) {
                return completions;
            }

            addIfMatches(completions, args[1], "java");
            addIfMatches(completions, args[1], "bedrock");
            return completions;
        }

        if (args.length > 1 && (args[0].equalsIgnoreCase("anouncment") || args[0].equalsIgnoreCase("announcement"))) {
            if (!hasVipPlus(sender)) {
                return completions;
            }

            addColorTagSuggestions(completions, args[args.length - 1], false, sender.getName());
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("pet")) {
            addIfMatches(completions, args[1], "off");
            for (String pet : getPetSuggestions(sender)) {
                addIfMatches(completions, args[1], pet);
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("particles")) {
            addIfMatches(completions, args[1], "rgb");
            addIfMatches(completions, args[1], "end_rod");
            addIfMatches(completions, args[1], "heart");
            addIfMatches(completions, args[1], "fire");
            addIfMatches(completions, args[1], "glowing");
            addIfMatches(completions, args[1], "petals");
            addIfMatches(completions, args[1], "off");
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("nick_color")) {
            addIfMatches(completions, args[1], "off");
            addIfMatches(completions, args[1], "black");
            addIfMatches(completions, args[1], "dark_blue");
            addIfMatches(completions, args[1], "dark_green");
            addIfMatches(completions, args[1], "dark_aqua");
            addIfMatches(completions, args[1], "dark_red");
            addIfMatches(completions, args[1], "dark_purple");
            addIfMatches(completions, args[1], "gold");
            addIfMatches(completions, args[1], "gray");
            addIfMatches(completions, args[1], "grey");
            addIfMatches(completions, args[1], "dark_gray");
            addIfMatches(completions, args[1], "dark_grey");
            addIfMatches(completions, args[1], "blue");
            addIfMatches(completions, args[1], "green");
            addIfMatches(completions, args[1], "aqua");
            addIfMatches(completions, args[1], "red");
            addIfMatches(completions, args[1], "light_purple");
            addIfMatches(completions, args[1], "yellow");
            addIfMatches(completions, args[1], "white");
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("chatcolor")) {
            addIfMatches(completions, args[1], "off");
            addColorOnlyTagSuggestions(completions, args[1]);
            return completions;
        }

        if (args.length > 1 && (args[0].equalsIgnoreCase("prefix") || args[0].equalsIgnoreCase("suffix"))) {
            if (args.length >= 2 && args[1].equalsIgnoreCase("off")) {
                return completions;
            }

            String current = args[args.length - 1];
            if (args.length == 2) {
                addIfMatches(completions, current, "off");
            }

            addColorTagSuggestions(completions, current, false, sender.getName());
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("particles") && args[1].equalsIgnoreCase("petals")) {
            addIfMatches(completions, args[2], "1");
            addIfMatches(completions, args[2], "2");
            addIfMatches(completions, args[2], "3");
            addIfMatches(completions, args[2], "4");
            addIfMatches(completions, args[2], "5");
            addIfMatches(completions, args[2], "6");
            addIfMatches(completions, args[2], "7");
            addIfMatches(completions, args[2], "8");
            addIfMatches(completions, args[2], "9");
            addIfMatches(completions, args[2], "10");
            return completions;
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("rename")) {
            if (!hasVipPlus(sender)) {
                return completions;
            }

            addColorTagSuggestions(completions, args[args.length - 1], false, sender.getName());
            return completions;
        }

        if (args.length > 1 && (args[0].equalsIgnoreCase("join_message") || args[0].equalsIgnoreCase("leave_message"))) {
            if (args.length >= 2 && args[1].equalsIgnoreCase("off")) {
                return completions;
            }

            String current = args[args.length - 1];
            if (args.length == 2) {
                addIfMatches(completions, current, "off");
            }

            addColorTagSuggestions(completions, current, true, sender.getName());
            return completions;
        }

        return completions;
    }

    private String[] getPetSuggestions(CommandSender sender) {
        if (sender instanceof Player player) {
            String locale = player.getLocale();
            if (locale != null && (locale.toLowerCase(Locale.ROOT).startsWith("uk") || locale.toLowerCase(Locale.ROOT).startsWith("ua"))) {
                return PETS_UA;
            }
        }

        return PETS_EN;
    }

    private void addColorTagSuggestions(List<String> completions, String current, boolean includeName, String senderName) {
        if (includeName) {
            addIfMatches(completions, current, senderName);
        }
        completions.add("#black#");
        completions.add("#dark_blue#");
        completions.add("#dark_green#");
        completions.add("#dark_aqua#");
        completions.add("#dark_red#");
        completions.add("#dark_purple#");
        completions.add("#gold#");
        completions.add("#gray#");
        completions.add("#grey#");
        completions.add("#dark_gray#");
        completions.add("#dark_grey#");
        completions.add("#blue#");
        completions.add("#green#");
        completions.add("#aqua#");
        completions.add("#red#");
        completions.add("#light_purple#");
        completions.add("#yellow#");
        completions.add("#white#");

        completions.add("#bold#");
        completions.add("#italic#");
        completions.add("#underline#");
        completions.add("#strikethrough#");
        completions.add("#magic#");
        completions.add("#reset#");

        completions.add("#ff00ff#");
    }

    private void addColorOnlyTagSuggestions(List<String> completions, String current) {
        addIfMatches(completions, current, "#black#");
        addIfMatches(completions, current, "#dark_blue#");
        addIfMatches(completions, current, "#dark_green#");
        addIfMatches(completions, current, "#dark_aqua#");
        addIfMatches(completions, current, "#dark_red#");
        addIfMatches(completions, current, "#dark_purple#");
        addIfMatches(completions, current, "#gold#");
        addIfMatches(completions, current, "#gray#");
        addIfMatches(completions, current, "#grey#");
        addIfMatches(completions, current, "#dark_gray#");
        addIfMatches(completions, current, "#dark_grey#");
        addIfMatches(completions, current, "#blue#");
        addIfMatches(completions, current, "#green#");
        addIfMatches(completions, current, "#aqua#");
        addIfMatches(completions, current, "#red#");
        addIfMatches(completions, current, "#light_purple#");
        addIfMatches(completions, current, "#yellow#");
        addIfMatches(completions, current, "#white#");
        addIfMatches(completions, current, "#ff00ff#");
    }

    private void addIfMatches(List<String> completions, String input, String value) {
        if (value.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT))) {
            completions.add(value);
        }
    }

    private boolean hasVipPlus(CommandSender sender) {
        return sender.hasPermission("vip.vip_plus");
    }
}
