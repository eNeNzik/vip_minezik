package com.eNeNzik.vip;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public final class Vip extends JavaPlugin {

    private static final long VIP_PROMOTION_INTERVAL_TICKS = 60L * 60L * 20L;
    private static Vip instance;

    private ParticleManager particleManager;
    private NickColorManager nickColorManager;
    private File playerDataFile;
    private FileConfiguration playerData;
    private VipCommand vipCommandExecutor;
    private LangManager langManager;
    private TabListManager tabListManager;
    private VipMenuManager vipMenuManager;
    private final VipTabCompleter vipTabCompleter = new VipTabCompleter();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        langManager = new LangManager(this);
        setupPlayerData();

        particleManager = new ParticleManager(this);
        nickColorManager = new NickColorManager(this);
        tabListManager = new TabListManager(this, nickColorManager);
        vipMenuManager = new VipMenuManager(this);
        vipCommandExecutor = new VipCommand(this, particleManager, nickColorManager);

        registerVipRecipes();
        startVipWelcomeCheckTask();
        // startVipPromotionTask();
        startVipStateCleanupTask();

        tabListManager.start();
        vipMenuManager.startCleanupTask();
        vipCommandExecutor.startPetCheckTask(this);
        vipCommandExecutor.startKitPreviewCleanupTask(this);

        getServer().getPluginManager().registerEvents(
                new PlayerListener(this, particleManager, nickColorManager, vipCommandExecutor),
                this
        );
        getServer().getPluginManager().registerEvents(vipMenuManager, this);
        getServer().getPluginManager().registerEvents(new LocalChatListener(this, nickColorManager), this);
    }

    private void registerVipRecipes() {
        NamespacedKey lightKey = new NamespacedKey(this, "vip_light");

        Bukkit.removeRecipe(lightKey);

        ShapedRecipe lightRecipe = new ShapedRecipe(lightKey, new ItemStack(Material.LIGHT, 4));
        lightRecipe.shape(
                "GGG",
                "GTG",
                "GGG"
        );
        lightRecipe.setIngredient('G', Material.GLOWSTONE);
        lightRecipe.setIngredient('T', Material.AMETHYST_SHARD);

        Bukkit.addRecipe(lightRecipe);
    }

    private void setupPlayerData() {
        playerDataFile = new File(getDataFolder(), "playerdata.yml");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public FileConfiguration getPlayerData() {
        return playerData;
    }

    public void savePlayerData() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (tabListManager != null) {
            tabListManager.stop();
        }
        if (vipMenuManager != null) {
            vipMenuManager.stopCleanupTask();
        }
        if (vipCommandExecutor != null) {
            vipCommandExecutor.saveAllPets();
            vipCommandExecutor.removeAllPets();
        }
        if (particleManager != null) {
            particleManager.saveAllStyles();
            particleManager.shutdown();
        }
        savePlayerData();
        instance = null;
    }

    public LangManager getLang() {
        return langManager;
    }

    public VipMenuManager getVipMenuManager() {
        return vipMenuManager;
    }

    public void startVipWelcomeCheckTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkVipWelcome(player);
            }
        }, 100L, 100L);
    }

    private void startVipPromotionTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendVipPromotion(player);
            }
        }, VIP_PROMOTION_INTERVAL_TICKS, VIP_PROMOTION_INTERVAL_TICKS);
    }

    private void startVipStateCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("vip.vip") || player.hasPermission("vip.vip_plus")) {
                    continue;
                }

                nickColorManager.removeSavedNickStyle(player);
                particleManager.stopParticles(player, false, false);
                vipCommandExecutor.removePet(player);
            }
        }, 20L * 20L, 20L * 20L);
    }

    public void sendVipPromotion(Player player) {
        if (player.hasPermission("vip.vip") || player.hasPermission("vip.vip_plus")) {
            return;
        }
        player.sendMessage(getLang().get(player, "vip-promotion.chat1"));
        player.sendMessage(getLang().get(player, "vip-promotion.chat2"));
        TextComponent link = new TextComponent(getLang().get(player, "vip-message.link-text"));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minezik.com.ua/"));
        link.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(getLang().get(player, "vip-message.site-hover")).create()
        ));
        player.spigot().sendMessage(link);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8F, 1.1F);
    }

    private void checkVipWelcome(Player player) {
        if (!player.hasPermission("vip.vip") && !player.hasPermission("vip.vip_plus")) {
            return;
        }

        String path = "players." + player.getUniqueId() + ".vip-welcome-received";

        if (getPlayerData().getBoolean(path, false)) {
            return;
        }

        player.sendMessage(getLang().get(player, "vip-welcome.thanks"));

        TextComponent help = new TextComponent(getLang().get(player, "vip-welcome.help-before"));
        TextComponent command = new TextComponent(getLang().get(player, "vip-welcome.help-command"));

        command.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/vip help"
        ));

        command.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(getLang().get(player, "vip-welcome.help-hover")).create()
        ));

        help.addExtra(command);
        help.addExtra(new TextComponent(getLang().get(player, "vip-welcome.help-after")));

        player.spigot().sendMessage(help);
        player.playSound(
                player.getLocation(),
                Sound.UI_TOAST_CHALLENGE_COMPLETE,
                0.8f,
                1.2f
        );
        getPlayerData().set(path, true);
        savePlayerData();
    }

    static boolean dispatchVipCommand(CommandSender sender, String[] args) {
        Vip plugin = instance;
        if (plugin == null || plugin.vipCommandExecutor == null) {
            sender.sendMessage(ChatColor.RED + "VIP plugin is still loading.");
            return true;
        }

        return plugin.vipCommandExecutor.onCommand(sender, null, "vip", args);
    }

    static Collection<String> completeVipCommand(CommandSender sender, String[] args) {
        Vip plugin = instance;
        if (plugin == null || plugin.vipCommandExecutor == null) {
            return List.of();
        }

        if (args.length == 0) {
            args = new String[]{""};
        }

        return plugin.vipTabCompleter.onTabComplete(sender, null, "vip", args);
    }

    static boolean toggleChatMode(CommandSender sender) {
        Vip plugin = instance;
        if (plugin == null) {
            sender.sendMessage(ChatColor.RED + "VIP plugin is still loading.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLang().get(sender, "common.only-player"));
            return true;
        }

        String path = "players." + player.getUniqueId() + ".chat-local-default";
        boolean localByDefault = !plugin.getPlayerData().getBoolean(path, false);
        plugin.getPlayerData().set(path, localByDefault);
        plugin.savePlayerData();

        player.sendMessage(plugin.getLang().get(
                player,
                localByDefault ? "chat-toggle.local-default" : "chat-toggle.global-default"
        ));
        return true;
    }
}
