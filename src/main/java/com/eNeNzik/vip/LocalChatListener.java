package com.eNeNzik.vip;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class LocalChatListener implements Listener {

    private static final double LOCAL_RADIUS = 100.0;
    private static final double LOCAL_RADIUS_SQUARED = LOCAL_RADIUS * LOCAL_RADIUS;

    private final Vip plugin;
    private final NickColorManager nickColorManager;
    private final SafeLoginHook safeLoginHook;

    public LocalChatListener(Vip plugin, NickColorManager nickColorManager) {
        this.plugin = plugin;
        this.nickColorManager = nickColorManager;
        this.safeLoginHook = new SafeLoginHook(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player sender = event.getPlayer();
        String rawMessage = event.getMessage();

        Bukkit.getScheduler().runTask(plugin, () -> sendChat(sender, rawMessage));
    }

    private void sendChat(Player sender, String rawMessage) {
        if (!sender.isOnline()) {
            return;
        }

        if (safeLoginHook.shouldBlockChat(sender)) {
            return;
        }

        boolean global = rawMessage.startsWith("!");
        String message = global ? rawMessage.substring(1).trim() : rawMessage;

        if (message.isBlank()) {
            return;
        }

        if (sender.hasPermission("vip.vip") || sender.hasPermission("vip.vip_plus")) {
            String translatedMessage = ColorTokens.translateHashColors(message);
            boolean hasColorTags = !translatedMessage.equals(message);

            if (hasColorTags) {
                message = translatedMessage;
            } else {
                String savedChatColor = plugin.getPlayerData().getString("players." + sender.getUniqueId() + ".chat-color");
                if (savedChatColor != null && !savedChatColor.isBlank()) {
                    message = savedChatColor + message;
                }
            }
        }

        boolean someoneNearby = false;

        for (Player receiver : Bukkit.getOnlinePlayers()) {
            if (!global) {
                if (!receiver.getWorld().equals(sender.getWorld())) {
                    continue;
                }

                if (receiver.getLocation().distanceSquared(sender.getLocation()) > LOCAL_RADIUS_SQUARED) {
                    continue;
                }

                if (!receiver.equals(sender)) {
                    someoneNearby = true;
                }
            }

            receiver.spigot().sendMessage(createChatLine(receiver, sender, message, global));
        }

        Bukkit.getConsoleSender().sendMessage(
                ChatColor.GRAY + (global ? "[Global] " : "[Local] ")
                        + sender.getName() + ": " + ChatColor.stripColor(message)
        );

        if (!global && !someoneNearby) {
            sender.sendMessage(plugin.getLang().get(sender, "local-chat.no-one-heard"));
        }
    }

    private TextComponent createChatLine(Player receiver, Player sender, String message, boolean global) {
        TextComponent line = new TextComponent();

        addClickableName(line, receiver, sender);

        TextComponent separator = new TextComponent(global ? ": " : ">> ");
        separator.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        line.addExtra(separator);

        for (BaseComponent part : TextComponent.fromLegacyText(ChatColor.WHITE + message)) {
            line.addExtra(part);
        }

        return line;
    }

    private void addClickableName(TextComponent line, Player receiver, Player sender) {
        ClickEvent clickEvent = new ClickEvent(
                ClickEvent.Action.SUGGEST_COMMAND,
                "/msg " + sender.getName() + " "
        );
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(plugin.getLang().get(
                        receiver,
                        "local-chat.msg-hover",
                        "%player%",
                        sender.getName()
                ))
                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                        .create()
        );

        for (BaseComponent part : TextComponent.fromLegacyText(createStyledName(sender))) {
            part.setClickEvent(clickEvent);
            part.setHoverEvent(hoverEvent);
            line.addExtra(part);
        }
    }

    private String createStyledName(Player sender) {
        if (!hasVip(sender)) {
            return sender.getName();
        }

        String prefix = nickColorManager.getSavedPrefix(sender.getUniqueId());
        String suffix = nickColorManager.getSavedSuffix(sender.getUniqueId());
        ChatColor color = nickColorManager.getSavedColor(sender.getUniqueId());

        return (prefix != null ? prefix : "")
                + (color != null ? color : ChatColor.WHITE)
                + sender.getName()
                + ChatColor.RESET
                + (suffix != null ? suffix : "");
    }

    private boolean hasVip(Player player) {
        return player.hasPermission("vip.vip") || player.hasPermission("vip.vip_plus");
    }
}
