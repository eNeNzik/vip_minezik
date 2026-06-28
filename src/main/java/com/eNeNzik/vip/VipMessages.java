package com.eNeNzik.vip;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class VipMessages {

    private VipMessages() {
    }

    public static void sendVipMessage(Vip plugin, CommandSender sender) {
        TextComponent link = new TextComponent(plugin.getLang().get(sender, "vip-message.link-text"));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minezik.com.ua/"));
        link.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(plugin.getLang().get(sender, "vip-message.site-hover")).create()
        ));

        TextComponent fullHelpCommand = new TextComponent(plugin.getLang().get(sender, "vip-message.help-before"));
        TextComponent helpCommand = new TextComponent(plugin.getLang().get(sender, "vip-message.help-command"));
        TextComponent lastHelpCommandPart = new TextComponent(plugin.getLang().get(sender, "vip-message.help-after"));

        helpCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vip help"));
        helpCommand.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(plugin.getLang().get(sender, "vip-message.help-hover")).create()
        ));

        fullHelpCommand.addExtra(helpCommand);
        fullHelpCommand.addExtra(lastHelpCommandPart);
        if (sender.hasPermission("vip.vip")){
            sender.sendMessage(plugin.getLang().get(sender, "vip-message.vip"));
        }else if(sender.hasPermission("vip.vip_plus")){
            sender.sendMessage(plugin.getLang().get(sender, "vip-message.vip+"));
        }else {
            sender.sendMessage(plugin.getLang().get(sender, "vip-message.buy"));
        }
        if (sender instanceof Player player) {
            player.spigot().sendMessage(link);
            player.spigot().sendMessage(fullHelpCommand);
        } else {
            sender.sendMessage(plugin.getLang().get(sender, "vip-message.console-site"));
            sender.sendMessage(plugin.getLang().get(sender, "vip-message.console-help"));
        }
    }

    public static void sendHelp(Vip plugin, CommandSender sender) {
        for (String line : plugin.getLang().getList(sender, "help.lines")) {
            sender.sendMessage(line);
        }
    }
}
