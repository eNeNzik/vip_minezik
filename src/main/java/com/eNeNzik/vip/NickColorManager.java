package com.eNeNzik.vip;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Locale;
import java.util.UUID;

public final class NickColorManager {

    private final Vip plugin;

    public NickColorManager(Vip plugin) {
        this.plugin = plugin;
    }

    public void applySavedNickStyle(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            applyForViewer(viewer, target);
        }
    }
    public void applySavedNickStyleForViewer(Player viewer) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            applyForViewer(viewer, target);
        }
    }

    public void removeSavedNickStyle(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            removeFromViewer(viewer, target);
        }
    }

    public void saveNickColor(UUID uuid, ChatColor color) {
        plugin.getPlayerData().set("players." + uuid + ".nick-color", color == null ? null : color.name());
    }

    public void savePrefix(UUID uuid, String prefix) {
        plugin.getPlayerData().set("players." + uuid + ".nick-prefix", (prefix == null || prefix.isBlank()) ? null : prefix);
    }

    public void saveSuffix(UUID uuid, String suffix) {
        plugin.getPlayerData().set("players." + uuid + ".nick-suffix", (suffix == null || suffix.isBlank()) ? null : suffix);
    }

    public ChatColor getSavedColor(UUID uuid) {
        String name = plugin.getPlayerData().getString("players." + uuid + ".nick-color");
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            ChatColor c = ChatColor.valueOf(name.toUpperCase(Locale.ROOT));
            return c.isColor() ? c : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public String getSavedPrefix(UUID uuid) {
        String s = plugin.getPlayerData().getString("players." + uuid + ".nick-prefix");
        if (s == null || s.isBlank()) {
            return null;
        }
        return s;
    }

    public String getSavedSuffix(UUID uuid) {
        String s = plugin.getPlayerData().getString("players." + uuid + ".nick-suffix");
        if (s == null || s.isBlank()) {
            return null;
        }
        return s;
    }

    private void applyForViewer(Player viewer, Player target) {
        ChatColor color = getSavedColor(target.getUniqueId());
        String prefix = getSavedPrefix(target.getUniqueId());
        String suffix = getSavedSuffix(target.getUniqueId());

        if (color == null && (prefix == null || prefix.isEmpty()) && (suffix == null || suffix.isEmpty())) {
            removeFromViewer(viewer, target);
            return;
        }

        Scoreboard sb = viewer.getScoreboard();
        String teamName = teamNameFor(target.getUniqueId());

        Team team = sb.getTeam(teamName);
        if (team == null) {
            team = sb.registerNewTeam(teamName);
        }
        // Each entry can only be in one team per scoreboard. Move it to our team.
        Team existing = sb.getEntryTeam(target.getName());
        if (existing != null && existing != team) {
            existing.removeEntry(target.getName());
        }
        // Keep name white if color wasn't explicitly set, but still allow prefix/suffix.
        team.setColor(color != null ? color : ChatColor.WHITE);
        team.setPrefix(prefix != null ? prefix : "");
        team.setSuffix(suffix != null ? suffix : "");
        team.addEntry(target.getName());
    }
    private void removeFromViewer(Player viewer, Player target) {
        Scoreboard sb = viewer.getScoreboard();
        Team existing = sb.getEntryTeam(target.getName());
        if (existing != null && existing.getName().startsWith("vnc")) {
            existing.removeEntry(target.getName());
            if (existing.getEntries().isEmpty()) {
                existing.unregister();
            }
        }
    }
    private String teamNameFor(UUID uuid) {
        // Team names are limited to 16 characters.
        String compact = uuid.toString().replace("-", "");
        return "vnc" + compact.substring(0, 13);
    }
}
