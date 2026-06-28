package com.eNeNzik.vip;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class TabListManager {

    private static final long UPDATE_INTERVAL_TICKS = 100L;

    private final Vip plugin;
    private final NickColorManager nickColorManager;
    private BukkitTask task;

    public TabListManager(Vip plugin, NickColorManager nickColorManager) {
        this.plugin = plugin;
        this.nickColorManager = nickColorManager;
    }

    public void start() {
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 0L, UPDATE_INTERVAL_TICKS);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        player.setPlayerListOrder(orderFor(player));
        player.setPlayerListName(tabNameFor(player));
        player.setPlayerListHeaderFooter(header(), ChatColor.GRAY + "ping: " + pingColor(player.getPing()) + player.getPing() + "ms");
    }

    private int orderFor(Player player) {
        if (player.isOp() || player.getName().equalsIgnoreCase("eNeNzik")) {
            return 0;
        }

        if (player.hasPermission("vip.vip_plus")) {
            return 1;
        }

        if (player.hasPermission("vip.vip")) {
            return 2;
        }

        return 3;
    }

    private String tabNameFor(Player player) {
        boolean hasVip = player.hasPermission("vip.vip") || player.hasPermission("vip.vip_plus");

        String prefix = "";
        String suffix = "";
        ChatColor nameColor = ChatColor.WHITE;

        if (hasVip) {
            String savedPrefix = nickColorManager.getSavedPrefix(player.getUniqueId());
            String savedSuffix = nickColorManager.getSavedSuffix(player.getUniqueId());
            ChatColor savedColor = nickColorManager.getSavedColor(player.getUniqueId());

            prefix = savedPrefix != null ? savedPrefix : "";
            suffix = savedSuffix != null ? savedSuffix : "";
            nameColor = savedColor != null ? savedColor : ChatColor.WHITE;
        }

        return prefix
                + nameColor
                + player.getName()
                + ChatColor.RESET
                + suffix
                + ChatColor.WHITE
                + " "
                + ChatColor.ITALIC
                + pingColor(player.getPing())
                + player.getPing()
                + "ms";
    }

    private String header() {
        return ChatColor.DARK_RED + "M"
                + ChatColor.GOLD + "I"
                + ChatColor.YELLOW + "N"
                + ChatColor.GREEN + "E"
                + ChatColor.BLUE + "Z"
                + ChatColor.DARK_BLUE + "I"
                + ChatColor.DARK_PURPLE + "K";
    }

    private ChatColor pingColor(int ping) {
        if (ping <= 70) {
            return ChatColor.GREEN;
        }

        if (ping <= 150) {
            return ChatColor.YELLOW;
        }

        return ChatColor.RED;
    }
}
