package com.eNeNzik.vip;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class VipCommand implements CommandExecutor {

    private final Vip plugin;
    private final ParticleManager particleManager;
    private final NickColorManager nickColorManager;
    private final NamespacedKey kitPreviewItemKey;
    public final Map<UUID, Entity> pets = new HashMap<>();
    public final Map<UUID, String> petsName = new HashMap<>();
    private final Map<UUID, Long> lastAnnouncementTimes = new HashMap<>();
    private static final long ANNOUNCEMENT_COOLDOWN = 15 * 60 * 1000L;
    private static final long VIP_PLUS_KIT_COOLDOWN = 4L * 24 * 60 * 60 * 1000;
    private static final int[] VIP_PLUS_KIT_PREVIEW_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };
    public static final int VIP_PLUS_KIT_CLAIM_SLOT = 48;
    public static final int VIP_PLUS_KIT_CANCEL_SLOT = 50;

    public VipCommand(Vip plugin, ParticleManager particleManager, NickColorManager nickColorManager) {
        this.plugin = plugin;
        this.particleManager = particleManager;
        this.nickColorManager = nickColorManager;
        this.kitPreviewItemKey = new NamespacedKey(plugin, "kit_preview_item");
        loadSavedPets();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.getLang().setActiveSender(sender);
        try {
            return handleCommand(sender, command, label, args);
        } finally {
            plugin.getLang().clearActiveSender();
        }
    }

    private boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (hasVip(player)) {
                    plugin.getVipMenuManager().openMainMenu(player);
                } else {
                    VipMessages.sendVipMessage(plugin, player);
                }
                return true;
            }

            VipMessages.sendVipMessage(plugin, sender);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            VipMessages.sendHelp(plugin, sender);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("quick_action")) {
            if (sender instanceof Player player && hasVip(player)) {
                plugin.getVipMenuManager().openMainMenu(player);
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("close_dialog")) {
            if (sender instanceof Player player) {
                player.closeDialog();
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("menu")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            if (!hasVip(player)) {
                VipMessages.sendVipMessage(plugin, player);
                return true;
            }

            plugin.getVipMenuManager().openMenu(player, args[1]);
            return true;
        }

        if (args[0].equalsIgnoreCase("language") || args[0].equalsIgnoreCase("lang")) {
            return handleLanguageCommand(sender, args);
        }

        if (!hasVip(sender)) {
            sender.sendMessage(plugin.getLang().get("common.only-vip"));
            VipMessages.sendVipMessage(plugin, sender);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("whitelist")) {
            if (!hasVipPlus(sender)) {
                sender.sendMessage(plugin.getLang().get("common.only-vip-plus"));
                return true;
            }

            sender.sendMessage(plugin.getLang().get("add-friend.usage"));
            sender.sendMessage(plugin.getLang().get("add-friend.info"));
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("addFriend")) {
            if (!hasVipPlus(sender)) {
                sender.sendMessage(plugin.getLang().get("common.only-vip-plus"));
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            String friendName = args[2];
            if (!friendName.matches("[A-Za-z0-9_]{3,16}")) {
                player.sendMessage(plugin.getLang().get("add-friend.invalid-name"));
                return true;
            }

            UUID uuid = player.getUniqueId();
            String basePath = "players." + uuid + ".weekly-whitelist";
            long now = Instant.now().getEpochSecond();
            long resetAt = plugin.getPlayerData().getLong(basePath + ".reset-at", 0);

            if (resetAt == 0 || now >= resetAt) {
                plugin.getPlayerData().set(basePath + ".reset-at", Instant.now().plus(7, ChronoUnit.DAYS).getEpochSecond());
                plugin.getPlayerData().set(basePath + ".used", 0);
                plugin.getPlayerData().set(basePath + ".names", new ArrayList<String>());
            }

            int used = plugin.getPlayerData().getInt(basePath + ".used", 0);
            if (used >= 3) {
                player.sendMessage(plugin.getLang().get("add-friend.limit-reached"));
                return true;
            }

            List<String> names = plugin.getPlayerData().getStringList(basePath + ".names");
            for (String name : names) {
                if (name.equalsIgnoreCase(friendName)) {
                    player.sendMessage(plugin.getLang().get("add-friend.already-added"));
                    return true;
                }
            }

            if (args[1].equalsIgnoreCase("java")) {
                OfflinePlayer friend = Bukkit.getOfflinePlayer(friendName);

                if (friend.isWhitelisted()) {
                    player.sendMessage(plugin.getLang().get("add-friend.already-whitelisted"));
                    return true;
                }

                friend.setWhitelisted(true);
            } else if (args[1].equalsIgnoreCase("bedrock")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fwhitelist add " + friendName);
            } else {
                player.sendMessage(plugin.getLang().get("add-friend.invalid-type"));
                player.sendMessage(plugin.getLang().get("add-friend.usage"));
                return true;
            }

            names.add(friendName);
            plugin.getPlayerData().set(basePath + ".names", names);
            plugin.getPlayerData().set(basePath + ".used", used + 1);
            plugin.savePlayerData();

            player.sendMessage(plugin.getLang().get("add-friend.success", "%friend%", friendName));
            player.sendMessage(plugin.getLang().get("add-friend.remaining", "%count%", String.valueOf(2 - used)));
            return true;
        }

        if (args[0].equalsIgnoreCase("addFriend")) {
            if (!hasVipPlus(sender)) {
                sender.sendMessage(plugin.getLang().get("common.only-vip-plus"));
                return true;
            }

            sender.sendMessage(plugin.getLang().get("add-friend.usage"));
            sender.sendMessage(plugin.getLang().get("add-friend.info"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("kit")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            if (!hasVipPlus(player)) {
                player.sendMessage(plugin.getLang().get("kit.only-vip-plus"));
                return true;
            }

            long remaining = getVipPlusKitRemaining(player);
            if (remaining > 0) {
                player.sendMessage(plugin.getLang().get(
                        player,
                        "kit.cooldown",
                        "%time%", formatRemaining(player, remaining)
                ));
                return true;
            }

            openVipPlusKitPreview(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("anouncment") || args[0].equalsIgnoreCase("announcement")) {
            if (!sender.isOp() && !sender.hasPermission("vip.vip_plus")) {
                sender.sendMessage(plugin.getLang().get("announcement.no-permission"));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(plugin.getLang().get("announcement.usage"));
                sender.sendMessage(plugin.getLang().get("announcement.example"));
                return true;
            }

            String rawText = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String coloredText = ColorTokens.translateHashColors(rawText);

            if (ChatColor.stripColor(coloredText).trim().length() > 100) {
                sender.sendMessage(plugin.getLang().get("announcement.too-long"));
                sender.sendMessage(plugin.getLang().get("announcement.screen-warning"));
                return true;
            }

            if (sender instanceof Player player) {
                long now = System.currentTimeMillis();
                long lastUse = lastAnnouncementTimes.getOrDefault(player.getUniqueId(), 0L);
                long remaining = ANNOUNCEMENT_COOLDOWN - (now - lastUse);

                if (remaining > 0) {
                    long minutes = remaining / 1000 / 60;
                    long seconds = (remaining / 1000) % 60;

                    player.sendMessage(plugin.getLang().get("announcement.cooldown"));
                    player.sendMessage(plugin.getLang().get(
                            "announcement.wait",
                            "%minutes%", String.valueOf(minutes),
                            "%seconds%", String.valueOf(seconds)
                    ));
                    return true;
                }

                rememberAnnouncementUse(player.getUniqueId(), now);
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(plugin.getLang().get(
                        onlinePlayer,
                        "announcement.chat-format",
                        "%sender%", sender.getName(),
                        "%message%", coloredText
                ));
                onlinePlayer.sendTitle(
                        plugin.getLang().get(onlinePlayer, "announcement.title"),
                        coloredText,
                        10,
                        70,
                        20
                );
                onlinePlayer.playSound(
                        onlinePlayer.getLocation(),
                        Sound.UI_TOAST_CHALLENGE_COMPLETE,
                        0.8f,
                        1.2f
                );
            }
            Bukkit.getConsoleSender().sendMessage(plugin.getLang().get(
                    Bukkit.getConsoleSender(),
                    "announcement.chat-format",
                    "%sender%", sender.getName(),
                    "%message%", coloredText
            ));

            return true;
        }

        if (args[0].equalsIgnoreCase("pet")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            if (args.length == 2) {
                if (args[1].equalsIgnoreCase("off")) {
                    removePet(player);
                    petsName.remove(player.getUniqueId());
                    player.sendMessage(plugin.getLang().get("pet.disabled"));
                    return true;
                }

                createPet(args[1], player, true);
            } else {
                sender.sendMessage(plugin.getLang().get("pet.not-found"));
                player.sendMessage(plugin.getLang().get("pet.available"));
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("rename")) {
            if (!hasVipPlus(sender)) {
                sender.sendMessage(plugin.getLang().get("common.only-vip-plus"));
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(plugin.getLang().get("rename.no-name"));
                sender.sendMessage(plugin.getLang().get("rename.example"));
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                player.sendMessage(plugin.getLang().get("rename.no-item"));
                return true;
            }

            String rawName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String coloredName = ColorTokens.translateHashColors(rawName);
            if (ChatColor.stripColor(coloredName).trim().length() > 50) {
                sender.sendMessage(plugin.getLang().get("rename.too-long"));
                return true;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                player.sendMessage(plugin.getLang().get("rename.cannot-rename"));
                return true;
            }

            meta.setDisplayName(coloredName);
            item.setItemMeta(meta);
            player.sendMessage(plugin.getLang().get("rename.success", "%name%", coloredName));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("nick_color")) {
            sender.sendMessage(plugin.getLang().get("nick.usage"));
            sender.sendMessage(plugin.getLang().get("nick.colors"));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("nick_color")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            String colorName = args[1].toLowerCase(Locale.ROOT);
            if (colorName.equals("off")) {
                nickColorManager.saveNickColor(player.getUniqueId(), null);
                nickColorManager.applySavedNickStyle(player);
                player.sendMessage(plugin.getLang().get("nick.disabled"));
                return true;
            }

            ChatColor color = parseNamedColor(colorName);
            if (color == null) {
                player.sendMessage(plugin.getLang().get("nick.unknown"));
                return true;
            }

            nickColorManager.saveNickColor(player.getUniqueId(), color);
            nickColorManager.applySavedNickStyle(player);
            player.sendMessage(plugin.getLang().get(
                    "nick.set",
                    "%color%", color + color.name().toLowerCase(Locale.ROOT)
            ));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("chatcolor")) {
            sender.sendMessage(plugin.getLang().get("chat-color.usage"));
            sender.sendMessage(plugin.getLang().get("chat-color.example"));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("chatcolor")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            if (args[1].equalsIgnoreCase("off")) {
                plugin.getPlayerData().set("players." + player.getUniqueId() + ".chat-color", null);
                player.sendMessage(plugin.getLang().get("chat-color.disabled"));
                return true;
            }

            String colorCode = parseChatColorTag(args[1]);
            if (colorCode == null) {
                player.sendMessage(plugin.getLang().get("chat-color.invalid"));
                player.sendMessage(plugin.getLang().get("chat-color.example"));
                return true;
            }

            plugin.getPlayerData().set("players." + player.getUniqueId() + ".chat-color", colorCode);
            player.sendMessage(plugin.getLang().get("chat-color.saved", "%color%", colorCode + args[1]));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("prefix")) {
            sender.sendMessage(plugin.getLang().get("prefix.usage"));
            sender.sendMessage(plugin.getLang().get("prefix.example"));
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("prefix")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            if (args.length == 2 && args[1].equalsIgnoreCase("off")) {
                nickColorManager.savePrefix(player.getUniqueId(), null);
                nickColorManager.applySavedNickStyle(player);
                player.sendMessage(plugin.getLang().get("prefix.disabled"));
                return true;
            }

            String raw = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String colored = ColorTokens.translateHashColors(raw);
            if (ChatColor.stripColor(colored).length() > 10) {
                player.sendMessage(plugin.getLang().get("prefix.too-long"));
                return true;
            }

            nickColorManager.savePrefix(player.getUniqueId(), colored + ChatColor.RESET + " ");
            nickColorManager.applySavedNickStyle(player);
            player.sendMessage(plugin.getLang().get("prefix.saved"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("suffix")) {
            sender.sendMessage(plugin.getLang().get("suffix.usage"));
            sender.sendMessage(plugin.getLang().get("suffix.example"));
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("suffix")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            if (args.length == 2 && args[1].equalsIgnoreCase("off")) {
                nickColorManager.saveSuffix(player.getUniqueId(), null);
                nickColorManager.applySavedNickStyle(player);
                player.sendMessage(plugin.getLang().get("suffix.disabled"));
                return true;
            }

            String raw = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String colored = ColorTokens.translateHashColors(raw);
            if (ChatColor.stripColor(colored).length() > 10) {
                player.sendMessage(plugin.getLang().get("suffix.too-long"));
                return true;
            }

            nickColorManager.saveSuffix(player.getUniqueId(), colored + ChatColor.RESET + " ");
            nickColorManager.applySavedNickStyle(player);
            player.sendMessage(plugin.getLang().get("suffix.saved"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("join_message")) {
            sender.sendMessage(plugin.getLang().get("join-message.usage"));
            sender.sendMessage(plugin.getLang().get("join-message.example", "%player%", sender.getName()));
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("join_message")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            if (args.length == 2 && args[1].equalsIgnoreCase("off")) {
                plugin.getPlayerData().set("players." + player.getUniqueId() + ".join-message", null);
                player.sendMessage(plugin.getLang().get("join-message.disabled"));
                return true;
            }

            boolean hasName = false;
            for (int i = 1; i < args.length; i++) {
                if (ChatColor.stripColor(ColorTokens.translateHashColors(args[i])).equalsIgnoreCase(player.getName())) {
                    hasName = true;
                    break;
                }
            }

            String raw = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if (!hasName) {
                sender.sendMessage(plugin.getLang().get("join-message.must-contain-name"));
                return true;
            }

            String colored = ColorTokens.translateHashColors(raw);
            plugin.getPlayerData().set("players." + player.getUniqueId() + ".join-message", colored);
            player.sendMessage(plugin.getLang().get("join-message.saved", "%message%", colored));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("leave_message")) {
            sender.sendMessage(plugin.getLang().get("leave-message.usage"));
            sender.sendMessage(plugin.getLang().get("leave-message.example", "%player%", sender.getName()));
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("leave_message")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            if (args.length == 2 && args[1].equalsIgnoreCase("off")) {
                plugin.getPlayerData().set("players." + player.getUniqueId() + ".leave-message", null);
                player.sendMessage(plugin.getLang().get("leave-message.disabled"));
                return true;
            }

            boolean hasName = false;
            for (int i = 1; i < args.length; i++) {
                if (ChatColor.stripColor(ColorTokens.translateHashColors(args[i])).equalsIgnoreCase(player.getName())) {
                    hasName = true;
                    break;
                }
            }

            String raw = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if (!hasName) {
                sender.sendMessage(plugin.getLang().get("leave-message.must-contain-name"));
                return true;
            }

            String colored = ColorTokens.translateHashColors(raw);
            plugin.getPlayerData().set("players." + player.getUniqueId() + ".leave-message", colored);
            player.sendMessage(plugin.getLang().get("leave-message.saved", "%message%", colored));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.isOp()) {
                plugin.reloadConfig();
                plugin.getLang().load();
                sender.sendMessage(plugin.getLang().get("reload.success"));
            } else {
                sender.sendMessage(plugin.getLang().get("reload.no-permission"));
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("particles")) {
            sender.sendMessage(plugin.getLang().get("particles.usage"));
            sender.sendMessage(plugin.getLang().get("particles.petals-help"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("ender_chest")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("inventory.only-player"));
                return true;
            }

            player.openInventory(player.getEnderChest());
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("craft")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("inventory.only-player"));
                return true;
            }

            player.openWorkbench(player.getLocation(), true);
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("particles")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().get("common.only-player"));
                return true;
            }

            String styleName = args[1].toLowerCase(Locale.ROOT);

            switch (styleName) {
                case "rgb" -> {
                    particleManager.startParticleStyle(player, ParticleStyle.RGB, true);
                    return true;
                }
                case "end_rod" -> {
                    particleManager.startParticleStyle(player, ParticleStyle.END_ROD, true);
                    return true;
                }
                case "heart" -> {
                    particleManager.startParticleStyle(player, ParticleStyle.HEART, true);
                    return true;
                }
                case "fire" -> {
                    particleManager.startParticleStyle(player, ParticleStyle.FIRE, true);
                    return true;
                }
                case "glowing" -> {
                    particleManager.startParticleStyle(player, ParticleStyle.GLOWING, true);
                    return true;
                }
                case "petals" -> {
                    int petalsLevel = 5;

                    if (args.length >= 3) {
                        try {
                            petalsLevel = Integer.parseInt(args[2]);
                        } catch (NumberFormatException ignored) {
                            player.sendMessage(plugin.getLang().get("particles.level-number"));
                            return true;
                        }

                        if (petalsLevel < 1 || petalsLevel > 10) {
                            player.sendMessage(plugin.getLang().get("particles.level-range"));
                            return true;
                        }
                    }

                    particleManager.startPetals(player, petalsLevel, true);
                    return true;
                }
                case "off" -> {
                    particleManager.stopParticles(player, true, true);
                    return true;
                }
                default -> {
                    player.sendMessage(plugin.getLang().get("particles.unknown-style"));
                    return true;
                }
            }
        }

        sender.sendMessage(plugin.getLang().get("common.unknown-command"));
        return true;
    }

    private boolean handleLanguageCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLang().get("common.only-player"));
            return true;
        }

        if (args.length == 1) {
            player.sendMessage(plugin.getLang().get(
                    player,
                    "language.current",
                    "%language%",
                    plugin.getLang().getPlayerLangCode(player),
                    "%locale%",
                    player.getLocale() == null ? "unknown" : player.getLocale()
            ));
            player.sendMessage(plugin.getLang().get(player, "language.usage"));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.getLang().get(player, "language.usage"));
            return true;
        }

        String language = args[1].toLowerCase(Locale.ROOT);
        if (language.equals("auto")) {
            plugin.getLang().setPlayerLanguage(player, null);
            player.sendMessage(plugin.getLang().get(
                    player,
                    "language.auto",
                    "%language%",
                    plugin.getLang().getAutoLangCode(player),
                    "%locale%",
                    player.getLocale() == null ? "unknown" : player.getLocale()
            ));
            return true;
        }

        if (!plugin.getLang().isSupportedLanguage(language)) {
            player.sendMessage(plugin.getLang().get(player, "language.invalid"));
            player.sendMessage(plugin.getLang().get(player, "language.usage"));
            return true;
        }

        plugin.getLang().setPlayerLanguage(player, language);
        player.sendMessage(plugin.getLang().get(player, "language.changed", "%language%", language));
        return true;
    }

    public void removePet(Player player) {
        Entity pet = pets.remove(player.getUniqueId());
        if (pet != null && !pet.isDead()) {
            pet.remove();
        }
    }

    public void openVipPlusKitPreview(Player player) {
        KitPreviewHolder holder = new KitPreviewHolder();
        Inventory inventory = Bukkit.createInventory(holder, 54, plugin.getLang().get(player, "kit.preview-title"));
        holder.setInventory(inventory);

        ItemStack filler = markKitPreviewItem(createNamedItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(4, markKitPreviewItem(createVipPlusKitShulker(player)));

        List<ItemStack> contents = createVipPlusKitContents();
        for (int i = 0; i < contents.size() && i < VIP_PLUS_KIT_PREVIEW_SLOTS.length; i++) {
            inventory.setItem(VIP_PLUS_KIT_PREVIEW_SLOTS[i], markKitPreviewItem(contents.get(i)));
        }

        inventory.setItem(VIP_PLUS_KIT_CLAIM_SLOT, markKitPreviewItem(createNamedItem(
                Material.LIME_STAINED_GLASS_PANE,
                plugin.getLang().get(player, "kit.claim-button")
        )));
        inventory.setItem(VIP_PLUS_KIT_CANCEL_SLOT, markKitPreviewItem(createNamedItem(
                Material.RED_STAINED_GLASS_PANE,
                plugin.getLang().get(player, "kit.cancel-button")
        )));

        player.openInventory(inventory);
    }

    public void claimVipPlusKit(Player player) {
        removeDuplicatedKitPreviewItems(player);

        if (!hasVipPlus(player)) {
            player.closeInventory();
            player.sendMessage(plugin.getLang().get(player, "kit.only-vip-plus"));
            return;
        }

        long remaining = getVipPlusKitRemaining(player);
        if (remaining > 0) {
            player.closeInventory();
            player.sendMessage(plugin.getLang().get(
                    player,
                    "kit.cooldown",
                    "%time%", formatRemaining(player, remaining)
            ));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(plugin.getLang().get(player, "kit.no-space"));
            return;
        }

        long nextClaim = System.currentTimeMillis() + VIP_PLUS_KIT_COOLDOWN;
        plugin.getPlayerData().set(getVipPlusKitPath(player), nextClaim);
        plugin.savePlayerData();

        player.getInventory().addItem(createVipPlusKitShulker(player));
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        player.sendMessage(plugin.getLang().get(player, "kit.claimed"));
    }

    public boolean removeDuplicatedKitPreviewItems(Player player) {
        boolean removed = false;

        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (isKitPreviewItem(item)) {
                player.getInventory().setItem(slot, null);
                removed = true;
            }
        }

        if (isKitPreviewItem(player.getItemOnCursor())) {
            player.setItemOnCursor(null);
            removed = true;
        }

        return removed;
    }

    public boolean hasTaggedKitPreviewItem(ItemStack item) {
        return isKitPreviewItem(item);
    }

    private long getVipPlusKitRemaining(Player player) {
        long nextClaim = plugin.getPlayerData().getLong(getVipPlusKitPath(player), 0);
        return Math.max(0, nextClaim - System.currentTimeMillis());
    }

    private String getVipPlusKitPath(Player player) {
        return "players." + player.getUniqueId() + ".kits.vip-plus.next-claim";
    }

    private ItemStack createVipPlusKitShulker(Player player) {
        ItemStack shulker = new ItemStack(Material.PURPLE_SHULKER_BOX);
        ItemMeta itemMeta = shulker.getItemMeta();
        if (!(itemMeta instanceof BlockStateMeta meta)) {
            return shulker;
        }

        ShulkerBox box = (ShulkerBox) meta.getBlockState();
        for (ItemStack item : createVipPlusKitContents()) {
            box.getInventory().addItem(item.clone());
        }

        meta.setBlockState(box);
        meta.setDisplayName(plugin.getLang().get(player, "kit.shulker-name"));
        shulker.setItemMeta(meta);
        return shulker;
    }

    private ItemStack markKitPreviewItem(ItemStack item) {
        ItemStack marked = item.clone();
        ItemMeta meta = marked.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(kitPreviewItemKey, PersistentDataType.BYTE, (byte) 1);
            marked.setItemMeta(meta);
        }

        return marked;
    }

    private boolean isKitPreviewItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(kitPreviewItemKey, PersistentDataType.BYTE);
    }

    private ItemStack createKitPreviewInfo(Player player) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getLang().get(player, "kit.shulker-name"));
            meta.setLore(List.of(plugin.getLang().get(player, "kit.preview-only")));
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createSafePreviewItem(Player player, ItemStack reward) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + formatMaterialName(reward.getType()) + ChatColor.GRAY + " x" + reward.getAmount());
            meta.setLore(List.of(plugin.getLang().get(player, "kit.preview-only")));
            item.setItemMeta(meta);
        }

        return item;
    }

    private List<ItemStack> createVipPlusKitContents() {
        List<ItemStack> contents = new ArrayList<>();

        contents.add(mendingItem(Material.DIAMOND_HELMET));
        contents.add(mendingItem(Material.DIAMOND_CHESTPLATE));
        contents.add(mendingItem(Material.DIAMOND_LEGGINGS));
        contents.add(mendingItem(Material.DIAMOND_BOOTS));

        contents.add(mendingItem(Material.DIAMOND_SWORD));
        contents.add(mendingItem(Material.DIAMOND_PICKAXE));
        contents.add(mendingItem(Material.DIAMOND_AXE));
        contents.add(mendingItem(Material.DIAMOND_SHOVEL));

        contents.add(new ItemStack(Material.TOTEM_OF_UNDYING));
        contents.add(new ItemStack(Material.TOTEM_OF_UNDYING));
        contents.add(new ItemStack(Material.TOTEM_OF_UNDYING));
        contents.add(new ItemStack(Material.GOLDEN_CARROT, 32));
        contents.add(new ItemStack(Material.ENDER_PEARL, 16));
        contents.add(new ItemStack(Material.DIAMOND, 16));
        return contents;
    }
    private ItemStack mendingItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.addEnchant(Enchantment.MENDING, 1, true);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createFireworks(int amount, int power) {
        ItemStack fireworks = new ItemStack(Material.FIREWORK_ROCKET, amount);
        FireworkMeta meta = (FireworkMeta) fireworks.getItemMeta();

        if (meta != null) {
            meta.setPower(power);
            fireworks.setItemMeta(meta);
        }

        return fireworks;
    }

    private ItemStack createNamedItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }

        return item;
    }

    private String formatMaterialName(Material material) {
        StringBuilder name = new StringBuilder();

        for (String part : material.name().toLowerCase(Locale.ROOT).split("_")) {
            if (!name.isEmpty()) {
                name.append(' ');
            }

            name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }

        return name.toString();
    }

    private String formatRemaining(Player player, long millis) {
        long totalMinutes = Math.max(1, (millis + 59999) / 60000);
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        String locale = player.getLocale();
        if (locale != null && locale.toLowerCase(Locale.ROOT).startsWith("uk")) {
            return days + "д " + hours + "г " + minutes + "хв";
        }

        return days + "d " + hours + "h " + minutes + "m";
    }

    private ChatColor parseNamedColor(String input) {
        String key = input.toUpperCase(Locale.ROOT);
        key = key.replace("GREY", "GRAY");

        try {
            ChatColor color = ChatColor.valueOf(key);
            return color.isColor() ? color : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String parseChatColorTag(String input) {
        if (input == null || input.length() < 3 || !input.startsWith("#") || !input.endsWith("#")) {
            return null;
        }

        String token = input.substring(1, input.length() - 1).toLowerCase(Locale.ROOT);
        ChatColor namedColor = parseNamedColor(token);
        if (namedColor != null) {
            return namedColor.toString();
        }

        if (token.length() == 6 && token.matches("[0-9a-f]{6}")) {
            try {
                return net.md_5.bungee.api.ChatColor.of("#" + token).toString();
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return null;
    }

    private void rememberAnnouncementUse(UUID uuid, long now) {
        lastAnnouncementTimes.entrySet().removeIf(entry -> now - entry.getValue() > ANNOUNCEMENT_COOLDOWN);
        lastAnnouncementTimes.put(uuid, now);
    }

    public void startPetCheckTask(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, Entity>> iterator = pets.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, Entity> entry = iterator.next();
                    UUID uuid = entry.getKey();
                    Entity pet = entry.getValue();
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null || !player.isOnline()) {
                        if (pet != null && !pet.isDead()) {
                            pet.remove();
                        }
                        iterator.remove();
                        continue;
                    }

                    if (pet == null || pet.isDead() || !pet.isValid()) {
                        iterator.remove();
                        continue;
                    }

                    if (pet instanceof LivingEntity living) {
                        living.setRemainingAir(living.getMaximumAir());
                    }

                    if (pet instanceof Creeper creeper) {
                        creeper.setPowered(false);
                        creeper.setExplosionRadius(0);
                        creeper.setMaxFuseTicks(999999);
                    }

                    pet.setFireTicks(0);
                    if (!player.getPassengers().contains(pet)) {
                        pet.remove();
                        iterator.remove();
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 60L);
    }

    public void startKitPreviewCleanupTask(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (removeDuplicatedKitPreviewItems(player)) {
                        player.updateInventory();
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 60L);
    }

    public void removeAllPets() {
        for (Entity pet : pets.values()) {
            if (pet != null && !pet.isDead()) {
                pet.remove();
            }
        }
        pets.clear();
    }

    private int slimeSizeFromKey(String petKey) {
        return switch (petKey) {
            case "slime_small" -> 1;
            case "slime_medium" -> 2;
            case "slime_large" -> 4;
            default -> -1;
        };
    }

    private Salmon.Variant salmonVariantFromKey(String petKey) {
        return switch (petKey) {
            case "salmon_small" -> Salmon.Variant.SMALL;
            case "salmon_medium" -> Salmon.Variant.MEDIUM;
            case "salmon_large" -> Salmon.Variant.LARGE;
            default -> null;
        };
    }

    private DyeColor dyeColorFromKey(String petKey, String prefix) {
        try {
            return DyeColor.valueOf(petKey.substring(prefix.length()).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Axolotl.Variant axolotlVariantFromKey(String petKey) {
        try {
            return Axolotl.Variant.valueOf(petKey.substring("axolotl_".length()).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public void createPet(String petName, Player player, Boolean showMessage) {
        EntityType type = null;
        boolean isBaby = false;
        int slimeSize = 1;
        DyeColor sheepColor = DyeColor.WHITE;
        Salmon.Variant salmonVariant = Salmon.Variant.MEDIUM;
        Axolotl.Variant axolotlVariant = Axolotl.Variant.LUCY;
        String petKey = petName.toLowerCase(Locale.ROOT);

        if (petKey.startsWith("slime_")) {
            type = EntityType.SLIME;
            slimeSize = slimeSizeFromKey(petKey);
        } else if (petKey.startsWith("salmon_")) {
            type = EntityType.SALMON;
            salmonVariant = salmonVariantFromKey(petKey);
        } else if (petKey.startsWith("sheep_")) {
            sheepColor = dyeColorFromKey(petKey, "sheep_");
            if (sheepColor != null) {
                type = EntityType.SHEEP;
            }
        } else if (petKey.startsWith("axolotl_")) {
            axolotlVariant = axolotlVariantFromKey(petKey);
            if (axolotlVariant != null) {
                type = EntityType.AXOLOTL;
            }
        } else {
            switch (petKey) {
                case "елей":
                case "allay":
                    type = EntityType.ALLAY;
                    break;
                case "кішка":
                case "cat":
                    type = EntityType.CAT;
                    break;
                case "собака":
                case "dog":
                case "wolf":
                    type = EntityType.WOLF;
                    break;
                case "папуга":
                case "parrot":
                    type = EntityType.PARROT;
                    break;
                case "курка":
                case "chicken":
                    type = EntityType.CHICKEN;
                    break;
                case "корова":
                case "cow":
                    type = EntityType.COW;
                    break;
                case "свиня":
                case "pig":
                    type = EntityType.PIG;
                    break;
                case "вівця":
                case "sheep":
                    type = EntityType.SHEEP;
                    break;
                case "кінь":
                case "horse":
                    type = EntityType.HORSE;
                    break;
                case "осел":
                case "donkey":
                    type = EntityType.DONKEY;
                    break;
                case "лама":
                case "llama":
                    type = EntityType.LLAMA;
                    break;
                case "кролик":
                case "rabbit":
                    type = EntityType.RABBIT;
                    break;
                case "лисиця":
                case "fox":
                    type = EntityType.FOX;
                    break;
                case "панда":
                case "panda":
                    type = EntityType.PANDA;
                    break;
                case "бджола":
                case "bee":
                    type = EntityType.BEE;
                    break;
                case "коза":
                case "goat":
                    type = EntityType.GOAT;
                    break;
                case "жаба":
                case "frog":
                    type = EntityType.FROG;
                    break;
                case "верблюд":
                case "camel":
                    type = EntityType.CAMEL;
                    break;
                case "броненосець":
                case "armadillo":
                    type = EntityType.ARMADILLO;
                    break;
                case "черепаха":
                case "turtle":
                    type = EntityType.TURTLE;
                    break;
                case "черепашка":
                case "baby_turtle":
                    type = EntityType.TURTLE;
                    isBaby = true;
                    break;
                case "зомбі":
                case "zombie":
                    type = EntityType.ZOMBIE;
                    break;
                case "зомбі_дитинча":
                case "baby_zombie":
                    type = EntityType.ZOMBIE;
                    isBaby = true;
                    break;
                case "скелет":
                case "skeleton":
                    type = EntityType.SKELETON;
                    break;
                case "кріпер":
                case "creeper":
                    type = EntityType.CREEPER;
                    break;
                case "павук":
                case "spider":
                    type = EntityType.SPIDER;
                    break;
                case "ендермен":
                case "enderman":
                    type = EntityType.ENDERMAN;
                    break;
                case "відьма":
                case "witch":
                    type = EntityType.WITCH;
                    break;
                case "слизень":
                case "slime":
                    type = EntityType.SLIME;
                    break;
                case "фантом":
                case "phantom":
                    type = EntityType.PHANTOM;
                    break;
                case "хоглін":
                case "hoglin":
                    type = EntityType.HOGLIN;
                    isBaby = true;
                    break;
                case "піглін":
                case "piglin":
                    type = EntityType.PIGLIN;
                    break;
                case "зомбі_піглін":
                case "zombified_piglin":
                    type = EntityType.ZOMBIFIED_PIGLIN;
                    break;
                case "дитинча_лосося":
                case "small_salmon":
                case "salmon_small":
                    type = EntityType.SALMON;
                    salmonVariant = Salmon.Variant.SMALL;
                    break;
                case "лосось":
                case "salmon":
                case "salmon_medium":
                    type = EntityType.SALMON;
                    salmonVariant = Salmon.Variant.MEDIUM;
                    break;
                case "великий_лосось":
                case "large_salmon":
                case "salmon_large":
                    type = EntityType.SALMON;
                    salmonVariant = Salmon.Variant.LARGE;
                    break;
                case "рибка":
                case "tropical_fish":
                    type = EntityType.TROPICAL_FISH;
                    break;
                case "тріска":
                case "cod":
                    type = EntityType.COD;
                    break;
                case "іглобрюх":
                case "pufferfish":
                    type = EntityType.PUFFERFISH;
                    break;
                case "аксолотль":
                case "axolotl":
                    type = EntityType.AXOLOTL;
                    break;
                case "дельфін":
                case "dolphin":
                    type = EntityType.DOLPHIN;
                    break;
                case "кальмар":
                case "squid":
                    type = EntityType.SQUID;
                    break;
                case "світний_кальмар":
                case "glow_squid":
                    type = EntityType.GLOW_SQUID;
                    break;
                case "селянин":
                case "villager":
                    type = EntityType.VILLAGER;
                    break;
                case "бідний_селянин":
                case "baby_villager":
                    type = EntityType.VILLAGER;
                    isBaby = true;
                    break;
                case "залізний_голем":
                case "iron_golem":
                    type = EntityType.IRON_GOLEM;
                    break;
                case "сніговик":
                case "snow_golem":
                    type = EntityType.SNOW_GOLEM;
                    break;
            }
        }

        if (type == null || slimeSize < 1 || salmonVariant == null || axolotlVariant == null) {
            player.sendMessage(plugin.getLang().get(player, "pet.not-found"));
            player.sendMessage(plugin.getLang().get(player, "pet.available"));
            return;
        }

        removePet(player);
        petsName.put(player.getUniqueId(), petName);
        Entity pet = player.getWorld().spawnEntity(player.getLocation(), type);

        if (pet instanceof Villager villager) {
            villager.setVillagerType(Villager.Type.PLAINS);
            villager.setProfession(Villager.Profession.NONE);
        }

        if (pet instanceof Frog frog) {
            frog.setVariant(Frog.Variant.TEMPERATE);
        }

        if (pet instanceof Slime slime) {
            slime.setSize(slimeSize);
        }

        if (isBaby && pet instanceof Ageable ageable) {
            ageable.setBaby();
        }

        if (pet instanceof Sheep sheep) {
            sheep.setColor(sheepColor);
            sheep.setSheared(false);
        }

        if (pet instanceof Salmon salmon) {
            salmon.setVariant(salmonVariant);
        }

        if (pet instanceof Axolotl axolotl) {
            axolotl.setVariant(axolotlVariant);
        }

        if (pet instanceof LivingEntity living) {
            living.setAI(false);
            living.setCanPickupItems(false);

            EntityEquipment equipment = living.getEquipment();
            if (equipment != null) {
                equipment.clear();
            }
        }

        pet.setInvulnerable(true);
        pet.setFireTicks(0);
        pet.setSilent(true);
        pet.setGravity(false);
        pet.setPersistent(false);
        player.addPassenger(pet);
        pets.put(player.getUniqueId(), pet);

        if (showMessage) {
            String displayName = plugin.getLang().get(player, "pet-name." + petKey);
            if (displayName.equals("pet-name." + petKey)) {
                displayName = petName.toLowerCase(Locale.ROOT);
            }
            player.sendMessage(plugin.getLang().get(player, "pet.created", "%pet%", displayName));
        }
    }

    public void loadSavedPets() {
        ConfigurationSection playersSection = plugin.getPlayerData().getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }

        for (String uuidString : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                String petName = playersSection.getString(uuidString + ".pet");
                if (petName != null && !petName.isBlank()) {
                    petsName.put(uuid, petName);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void saveAllPets() {
        ConfigurationSection playersSection = plugin.getPlayerData().getConfigurationSection("players");

        if (playersSection != null) {
            for (String uuidString : playersSection.getKeys(false)) {
                String path = "players." + uuidString;
                plugin.getPlayerData().set(path + ".pet", null);
            }
        }

        for (UUID uuid : petsName.keySet()) {
            String path = "players." + uuid;
            plugin.getPlayerData().set(path + ".pet", petsName.get(uuid));
        }
    }

    private boolean hasVip(CommandSender sender) {
        return sender.hasPermission("vip.vip") || sender.hasPermission("vip.vip_plus");
    }

    private boolean hasVipPlus(CommandSender sender) {
        return sender.hasPermission("vip.vip_plus");
    }
}
