package com.eNeNzik.vip;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LangManager {

    private static final String DEFAULT_LANGUAGE = "en";

    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> languages = new HashMap<>();
    private final ThreadLocal<CommandSender> activeSender = new ThreadLocal<>();

    public LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        languages.clear();
        loadLanguage("en");
        loadLanguage("ua");
        loadLanguage("ru");
        loadLanguage("pl");
        loadLanguage("de");
    }

    public void setActiveSender(CommandSender sender) {
        activeSender.set(sender);
    }

    public void clearActiveSender() {
        activeSender.remove();
    }

    public String get(String path, String... replacements) {
        CommandSender sender = activeSender.get();
        if (sender != null) {
            return get(sender, path, replacements);
        }

        return get(DEFAULT_LANGUAGE, path, replacements);
    }

    public String get(CommandSender sender, String path, String... replacements) {
        if (sender instanceof Player player) {
            return get(player, path, replacements);
        }

        return get(DEFAULT_LANGUAGE, path, replacements);
    }

    public String get(Player player, String path, String... replacements) {
        return get(getPlayerLangCode(player), path, replacements);
    }

    public List<String> getList(String path, String... replacements) {
        CommandSender sender = activeSender.get();
        if (sender != null) {
            return getList(sender, path, replacements);
        }

        return getList(DEFAULT_LANGUAGE, path, replacements);
    }

    public List<String> getList(CommandSender sender, String path, String... replacements) {
        if (sender instanceof Player player) {
            return getList(getPlayerLangCode(player), path, replacements);
        }

        return getList(DEFAULT_LANGUAGE, path, replacements);
    }

    public String getPlayerLangCode(Player player) {
        String saved = getSavedLanguage(player);
        if (saved != null) {
            return saved;
        }

        return getAutoLangCode(player);
    }

    public String getAutoLangCode(Player player) {
        String locale = player.getLocale();
        if (locale == null) {
            return DEFAULT_LANGUAGE;
        }

        locale = locale.toLowerCase(Locale.ROOT);
        if (locale.startsWith("uk") || locale.startsWith("ua") || locale.contains("ukrain")) {
            return "ua";
        }

        if (locale.startsWith("ru")) {
            return "ru";
        }

        if (locale.startsWith("pl")) {
            return "pl";
        }

        if (locale.startsWith("de")) {
            return "de";
        }

        if (locale.startsWith("en")) {
            return "en";
        }

        return DEFAULT_LANGUAGE;
    }

    public boolean isSupportedLanguage(String code) {
        return code != null && languages.containsKey(code.toLowerCase(Locale.ROOT));
    }

    public void setPlayerLanguage(Player player, String code) {
        if (!(plugin instanceof Vip vip)) {
            return;
        }

        String path = "players." + player.getUniqueId() + ".language";
        if (code == null || code.equalsIgnoreCase("auto")) {
            vip.getPlayerData().set(path, null);
            vip.savePlayerData();
            return;
        }

        vip.getPlayerData().set(path, code.toLowerCase(Locale.ROOT));
        vip.savePlayerData();
    }

    private String getSavedLanguage(Player player) {
        if (!(plugin instanceof Vip vip)) {
            return null;
        }

        String saved = vip.getPlayerData().getString("players." + player.getUniqueId() + ".language");
        if (saved == null || saved.isBlank()) {
            return null;
        }

        saved = saved.toLowerCase(Locale.ROOT);
        return isSupportedLanguage(saved) ? saved : null;
    }

    private void loadLanguage(String code) {
        String resourcePath = "lang/" + code + ".yml";
        File file = new File(plugin.getDataFolder(), resourcePath);

        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        try (InputStream stream = plugin.getResource(resourcePath)) {
            if (stream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                );
                config.setDefaults(defaults);
                config.options().copyDefaults(true);
                config.save(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        languages.put(code, config);
    }

    private String get(String langCode, String path, String... replacements) {
        FileConfiguration file = fileFor(langCode);
        String message = file.getString(path, path);
        message = ChatColor.translateAlternateColorCodes('&', message);
        return applyReplacements(message, replacements);
    }

    private List<String> getList(String langCode, String path, String... replacements) {
        List<String> messages = new ArrayList<>();
        FileConfiguration file = fileFor(langCode);

        for (String message : file.getStringList(path)) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            messages.add(applyReplacements(message, replacements));
        }

        return messages;
    }

    private FileConfiguration fileFor(String langCode) {
        FileConfiguration file = languages.get(langCode);
        if (file != null) {
            return file;
        }

        FileConfiguration fallback = languages.get(DEFAULT_LANGUAGE);
        if (fallback != null) {
            return fallback;
        }

        throw new IllegalStateException("Default language file is not loaded: " + DEFAULT_LANGUAGE);
    }

    private String applyReplacements(String message, String... replacements) {
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return message;
    }

}
