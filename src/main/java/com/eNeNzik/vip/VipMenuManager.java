package com.eNeNzik.vip;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class VipMenuManager implements Listener {

    private static final PetOption[] PETS = {
            pet("allay", "ALLAY_SPAWN_EGG", Material.FEATHER),
            pet("cat", "CAT_SPAWN_EGG", Material.COD),
            pet("dog", "WOLF_SPAWN_EGG", Material.BONE),
            pet("parrot", "PARROT_SPAWN_EGG", Material.FEATHER),
            pet("chicken", "CHICKEN_SPAWN_EGG", Material.EGG),
            pet("cow", "COW_SPAWN_EGG", Material.LEATHER),
            pet("pig", "PIG_SPAWN_EGG", Material.PORKCHOP),
            petWithVariants("sheep", "SHEEP_SPAWN_EGG", Material.WHITE_WOOL),
            pet("horse", "HORSE_SPAWN_EGG", Material.SADDLE),
            pet("donkey", "DONKEY_SPAWN_EGG", Material.CHEST),
            pet("llama", "LLAMA_SPAWN_EGG", Material.LEAD),
            pet("rabbit", "RABBIT_SPAWN_EGG", Material.RABBIT_FOOT),
            pet("fox", "FOX_SPAWN_EGG", Material.SWEET_BERRIES),
            pet("panda", "PANDA_SPAWN_EGG", Material.BAMBOO),
            pet("bee", "BEE_SPAWN_EGG", Material.HONEYCOMB),
            pet("goat", "GOAT_SPAWN_EGG", Material.GOAT_HORN),
            pet("frog", "FROG_SPAWN_EGG", Material.OCHRE_FROGLIGHT),
            pet("camel", "CAMEL_SPAWN_EGG", Material.CACTUS),
            pet("armadillo", "ARMADILLO_SPAWN_EGG", Material.ARMADILLO_SCUTE),
            pet("turtle", "TURTLE_SPAWN_EGG", Material.TURTLE_EGG),
            pet("baby_turtle", "TURTLE_SPAWN_EGG", Material.TURTLE_EGG),
            pet("zombie", "ZOMBIE_SPAWN_EGG", Material.ROTTEN_FLESH),
            pet("baby_zombie", "ZOMBIE_SPAWN_EGG", Material.ROTTEN_FLESH),
            pet("skeleton", "SKELETON_SPAWN_EGG", Material.BONE),
            pet("creeper", "CREEPER_SPAWN_EGG", Material.GUNPOWDER),
            pet("spider", "SPIDER_SPAWN_EGG", Material.SPIDER_EYE),
            pet("enderman", "ENDERMAN_SPAWN_EGG", Material.ENDER_PEARL),
            pet("witch", "WITCH_SPAWN_EGG", Material.POTION),
            petWithVariants("slime", "SLIME_SPAWN_EGG", Material.SLIME_BALL),
            pet("phantom", "PHANTOM_SPAWN_EGG", Material.PHANTOM_MEMBRANE),
            pet("hoglin", "HOGLIN_SPAWN_EGG", Material.CRIMSON_FUNGUS),
            pet("piglin", "PIGLIN_SPAWN_EGG", Material.GOLD_INGOT),
            pet("zombified_piglin", "ZOMBIFIED_PIGLIN_SPAWN_EGG", Material.GOLDEN_SWORD),
            petWithVariants("salmon", "SALMON_SPAWN_EGG", Material.SALMON),
            pet("tropical_fish", "TROPICAL_FISH_SPAWN_EGG", Material.TROPICAL_FISH),
            pet("cod", "COD_SPAWN_EGG", Material.COD),
            pet("pufferfish", "PUFFERFISH_SPAWN_EGG", Material.PUFFERFISH),
            petWithVariants("axolotl", "AXOLOTL_SPAWN_EGG", Material.TROPICAL_FISH_BUCKET),
            pet("dolphin", "DOLPHIN_SPAWN_EGG", Material.PRISMARINE_CRYSTALS),
            pet("squid", "SQUID_SPAWN_EGG", Material.INK_SAC),
            pet("glow_squid", "GLOW_SQUID_SPAWN_EGG", Material.GLOW_INK_SAC),
            pet("villager", "VILLAGER_SPAWN_EGG", Material.EMERALD),
            pet("baby_villager", "VILLAGER_SPAWN_EGG", Material.EMERALD),
            pet("iron_golem", "IRON_GOLEM_SPAWN_EGG", Material.IRON_BLOCK),
            pet("snow_golem", "SNOW_GOLEM_SPAWN_EGG", Material.SNOW_BLOCK)
    };
    private static final String[] SLIME_VARIANTS = {"slime_small", "slime_medium", "slime_large"};
    private static final String[] SALMON_VARIANTS = {"salmon_small", "salmon_medium", "salmon_large"};
    private static final String[] AXOLOTL_VARIANTS = {
            "axolotl_lucy", "axolotl_wild", "axolotl_gold", "axolotl_cyan", "axolotl_blue"
    };
    private static final String[] SHEEP_VARIANTS = {
            "sheep_white", "sheep_orange", "sheep_magenta", "sheep_light_blue", "sheep_yellow", "sheep_lime",
            "sheep_pink", "sheep_gray", "sheep_light_gray", "sheep_cyan", "sheep_purple", "sheep_blue",
            "sheep_brown", "sheep_green", "sheep_red", "sheep_black"
    };

    private static final String[] NAMED_COLORS = {
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"
    };
    private static final int[] COLOR_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29
    };
    private static final DialogTag[] DIALOG_COLOR_TAGS = {
            tag("#black#", NamedTextColor.BLACK),
            tag("#dark_blue#", NamedTextColor.DARK_BLUE),
            tag("#dark_green#", NamedTextColor.DARK_GREEN),
            tag("#dark_aqua#", NamedTextColor.DARK_AQUA),
            tag("#dark_red#", NamedTextColor.DARK_RED),
            tag("#dark_purple#", NamedTextColor.DARK_PURPLE),
            tag("#gold#", NamedTextColor.GOLD),
            tag("#gray#", NamedTextColor.GRAY),
            tag("#grey#", NamedTextColor.GRAY),
            tag("#dark_gray#", NamedTextColor.DARK_GRAY),
            tag("#dark_grey#", NamedTextColor.DARK_GRAY),
            tag("#blue#", NamedTextColor.BLUE),
            tag("#green#", NamedTextColor.GREEN),
            tag("#aqua#", NamedTextColor.AQUA),
            tag("#red#", NamedTextColor.RED),
            tag("#light_purple#", NamedTextColor.LIGHT_PURPLE),
            tag("#yellow#", NamedTextColor.YELLOW),
            tag("#white#", NamedTextColor.WHITE),
            tag("#bold#", NamedTextColor.WHITE, TextDecoration.BOLD),
            tag("#italic#", NamedTextColor.WHITE, TextDecoration.ITALIC),
            tag("#underline#", NamedTextColor.WHITE, TextDecoration.UNDERLINED),
            tag("#underlined#", NamedTextColor.WHITE, TextDecoration.UNDERLINED),
            tag("#strikethrough#", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH),
            tag("#magic#", NamedTextColor.WHITE, TextDecoration.OBFUSCATED),
            tag("#obfuscated#", NamedTextColor.WHITE, TextDecoration.OBFUSCATED),
            tag("#reset#", NamedTextColor.WHITE)
    };
    private static final String DIALOG_TEXT_KEY = "text";

    private final Vip plugin;
    private final NamespacedKey menuActionKey;
    private BukkitTask cleanupTask;

    public VipMenuManager(Vip plugin) {
        this.plugin = plugin;
        this.menuActionKey = new NamespacedKey(plugin, "vip_menu_action");
    }

    private String tr(Player player, String path, String... replacements) {
        return plugin.getLang().get(player, path, replacements);
    }

    private Component legacy(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }

    private record DialogTag(String token, Component label) {
    }

    private record PetOption(String key, String translationKey, String eggName, Material fallback, boolean hasVariants) {
    }

    private static PetOption pet(String key, String eggName, Material fallback) {
        return new PetOption(key, "pet-name." + key, eggName, fallback, false);
    }

    private static PetOption petWithVariants(String key, String eggName, Material fallback) {
        return new PetOption(key, "pet-name." + key, eggName, fallback, true);
    }

    private static DialogTag tag(String token, NamedTextColor color, TextDecoration... decorations) {
        Component label = Component.text(token, color);
        for (TextDecoration decoration : decorations) {
            label = label.decorate(decoration);
        }
        return new DialogTag(token, label);
    }

    public void startCleanupTask() {
        stopCleanupTask();
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (removeDuplicatedMenuItems(player)) {
                    player.updateInventory();
                }
            }
        }, 60L, 60L);
    }

    public void stopCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
    }

    public void openMainMenu(Player player) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 54, tr(player, "menu.main-title"));
        holder.setInventory(inventory);
        fill(inventory);

        setButton(inventory, 10, Material.EMERALD, tr(player, "menu.buy"), "buy",
                tr(player, "menu.buy-lore"));
        setButton(inventory, 11, Material.BOOK, tr(player, "menu.help"), "cmd:vip help",
                tr(player, "menu.help-lore"));
        setButton(inventory, 12, Material.CHEST, tr(player, "menu.kit"), "cmd:vip kit",
                tr(player, "menu.kit-lore"));
        setButton(inventory, 13, Material.BLAZE_POWDER, tr(player, "menu.particles"), "menu:particles",
                tr(player, "menu.particles-lore"));
        setButton(inventory, 14, Material.SLIME_BALL, tr(player, "menu.pets"), "menu:pets",
                tr(player, "menu.pets-lore"));
        setButton(inventory, 15, Material.NAME_TAG, tr(player, "menu.name-style"), "menu:name",
                tr(player, "menu.name-style-lore"));
        setButton(inventory, 16, Material.OAK_SIGN, tr(player, "menu.join-leave"), "menu:messages",
                tr(player, "menu.join-leave-lore"));

        setButton(inventory, 19, Material.INK_SAC, tr(player, "menu.chat-color"), "menu:chat_color",
                tr(player, "menu.chat-color-lore"));
        setButton(inventory, 20, Material.WRITABLE_BOOK, tr(player, "menu.language"), "menu:language",
                tr(player, "menu.language-lore"));
        setButton(inventory, 21, Material.CRAFTING_TABLE, tr(player, "menu.craft"), "cmd:vip craft",
                tr(player, "menu.craft-lore"));
        setButton(inventory, 22, Material.ENDER_CHEST, tr(player, "menu.ender-chest"), "cmd:vip ender_chest",
                tr(player, "menu.ender-chest-lore"));
        setButton(inventory, 23, Material.ANVIL, tr(player, "menu.rename"), "anvil:rename",
                tr(player, "menu.rename-lore-1"),
                tr(player, "menu.rename-lore-2"));
        setButton(inventory, 24, Material.BELL, tr(player, "menu.announcement"), "chat:announcement",
                tr(player, "menu.announcement-lore-1"),
                tr(player, "menu.announcement-lore-2"));
        setButton(inventory, 25, Material.PLAYER_HEAD, tr(player, "menu.add-friend"), "menu:add_friend",
                tr(player, "menu.add-friend-lore"));

        setButton(inventory, 49, Material.BARRIER, tr(player, "menu.close"), "close");
        player.openInventory(inventory);
    }

    public void openMenu(Player player, String menu) {
        openSubMenu(player, menu);
    }

    private void openParticlesMenu(Player player) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 36, tr(player, "menu.particles-title"));
        holder.setInventory(inventory);
        fill(inventory);

        setButton(inventory, 10, Material.REDSTONE, ChatColor.RED + "RGB", "cmd:vip particles rgb");
        setButton(inventory, 11, Material.END_ROD, ChatColor.WHITE + "End rod", "cmd:vip particles end_rod");
        setButton(inventory, 12, Material.POPPY, ChatColor.LIGHT_PURPLE + "Heart", "cmd:vip particles heart");
        setButton(inventory, 13, Material.BLAZE_POWDER, ChatColor.GOLD + "Fire", "cmd:vip particles fire");
        setButton(inventory, 14, Material.PINK_PETALS, ChatColor.LIGHT_PURPLE + "Petals", "menu:petals");
        setButton(inventory, 15, Material.GLOW_INK_SAC, tr(player, "menu.particles-glowing"), "cmd:vip particles glowing");
        setButton(inventory, 16, Material.BARRIER, tr(player, "menu.disable"), "cmd:vip particles off");
        setBack(inventory, 31, player);
        player.openInventory(inventory);
    }

    private void openPetalsMenu(Player player) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 36, ChatColor.LIGHT_PURPLE + "Petals level");
        holder.setInventory(inventory);
        fill(inventory);

        for (int level = 1; level <= 10; level++) {
            setButton(inventory, 9 + level, Material.PINK_PETALS, ChatColor.LIGHT_PURPLE + "Petals " + level,
                    "cmd:vip particles petals " + level);
        }

        setBack(inventory, 31, player);
        player.openInventory(inventory);
    }

    private void openPetsMenu(Player player) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 54, tr(player, "menu.pets-title"));
        holder.setInventory(inventory);
        fill(inventory);

        setButton(inventory, 0, Material.BARRIER, tr(player, "menu.disable-pet"), "cmd:vip pet off");
        int slot = 1;
        for (PetOption pet : PETS) {
            if (slot >= 53) {
                break;
            }
            String action = pet.hasVariants() ? "petmenu:" + pet.key() : "cmd:vip pet " + pet.key();
            setButton(inventory, slot, spawnEgg(pet), tr(player, pet.translationKey()), action,
                    pet.hasVariants() ? tr(player, "menu.pet-variant-lore") : tr(player, "menu.pet-select-lore"));
            slot++;
        }

        setBack(inventory, 53, player);
        player.openInventory(inventory);
    }

    private void openPetVariantMenu(Player player, String petKey) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27,
                tr(player, "menu.pet-variant-title", "%pet%", tr(player, "pet-name." + petKey)));
        holder.setInventory(inventory);
        fill(inventory);

        switch (petKey) {
            case "slime" -> addVariantButtons(player, inventory, SLIME_VARIANTS, Material.SLIME_BALL);
            case "salmon" -> addVariantButtons(player, inventory, SALMON_VARIANTS, spawnEgg("SALMON_SPAWN_EGG", Material.SALMON));
            case "axolotl" -> addVariantButtons(player, inventory, AXOLOTL_VARIANTS, spawnEgg("AXOLOTL_SPAWN_EGG", Material.TROPICAL_FISH_BUCKET));
            case "sheep" -> addSheepVariantButtons(player, inventory);
            default -> {
                openPetsMenu(player);
                return;
            }
        }

        setButton(inventory, 22, Material.ARROW, tr(player, "menu.back"), "menu:pets");
        player.openInventory(inventory);
    }

    private void addVariantButtons(Player player, Inventory inventory, String[] variants, Material material) {
        int[] slots = {9, 11, 13, 15, 17};
        for (int i = 0; i < variants.length && i < slots.length; i++) {
            String variant = variants[i];
            setButton(inventory, slots[i], material, tr(player, "pet-name." + variant), "cmd:vip pet " + variant);
        }
    }

    private void addSheepVariantButtons(Player player, Inventory inventory) {
        for (int i = 0; i < SHEEP_VARIANTS.length && i < COLOR_SLOTS.length; i++) {
            String variant = SHEEP_VARIANTS[i];
            String color = variant.substring("sheep_".length());
            setButton(inventory, COLOR_SLOTS[i], woolForColor(color), tr(player, "pet-name." + variant),
                    "cmd:vip pet " + variant);
        }
    }

    private void openNameMenu(Player player) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 36, tr(player, "menu.name-title"));
        holder.setInventory(inventory);
        fill(inventory);

        setButton(inventory, 10, Material.LIME_DYE, tr(player, "menu.nick-color"), "menu:nick_color");
        setButton(inventory, 12, Material.PAPER, tr(player, "menu.prefix"), "anvil:prefix",
                tr(player, "menu.prefix-lore-1"),
                tr(player, "menu.prefix-lore-2"));
        setButton(inventory, 13, Material.BARRIER, tr(player, "menu.prefix-off"), "cmd:vip prefix off");
        setButton(inventory, 15, Material.PAPER, tr(player, "menu.suffix"), "anvil:suffix",
                tr(player, "menu.suffix-lore-1"));
        setButton(inventory, 16, Material.BARRIER, tr(player, "menu.suffix-off"), "cmd:vip suffix off");
        setBack(inventory, 31, player);
        player.openInventory(inventory);
    }

    private void openMessagesMenu(Player player) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 36, tr(player, "menu.messages-title"));
        holder.setInventory(inventory);
        fill(inventory);

        setButton(inventory, 11, Material.OAK_SIGN, tr(player, "menu.join-message"), "chat:join_message",
                tr(player, "menu.chat-input-lore"),
                tr(player, "menu.must-contain-name-lore"));
        setButton(inventory, 12, Material.BARRIER, tr(player, "menu.join-off"), "cmd:vip join_message off");
        setButton(inventory, 14, Material.SPRUCE_SIGN, tr(player, "menu.leave-message"), "chat:leave_message",
                tr(player, "menu.chat-input-lore"),
                tr(player, "menu.must-contain-name-lore"));
        setButton(inventory, 15, Material.BARRIER, tr(player, "menu.leave-off"), "cmd:vip leave_message off");
        setBack(inventory, 31, player);
        player.openInventory(inventory);
    }

    private void openNickColorMenu(Player player) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 36, tr(player, "menu.nick-color-title"));
        holder.setInventory(inventory);
        fill(inventory);

        setButton(inventory, 0, Material.BARRIER, tr(player, "menu.disable"), "cmd:vip nick_color off");
        addNamedColors(inventory, "vip nick_color ");
        setBack(inventory, 35, player);
        player.openInventory(inventory);
    }

    private void openChatColorMenu(Player player) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 36, tr(player, "menu.chat-color-title"));
        holder.setInventory(inventory);
        fill(inventory);

        setButton(inventory, 0, Material.BARRIER, tr(player, "menu.disable"), "cmd:vip chatcolor off");
        addNamedColors(inventory, "vip chatcolor #", "#");
        setButton(inventory, 26, Material.NAME_TAG, tr(player, "menu.custom-hex-color"), "anvil:chat_color",
                tr(player, "menu.hex-example"));
        setBack(inventory, 35, player);
        player.openInventory(inventory);
    }

    private void openLanguageMenu(Player player) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, tr(player, "menu.language-title"));
        holder.setInventory(inventory);
        fill(inventory);

        setButton(inventory, 10, Material.COMPASS, ChatColor.YELLOW + "Auto", "cmd:vip language auto");
        setButton(inventory, 11, Material.BLUE_WOOL, ChatColor.AQUA + "Українська", "cmd:vip language ua");
        setButton(inventory, 12, Material.WHITE_WOOL, ChatColor.WHITE + "English", "cmd:vip language en");
        setButton(inventory, 13, Material.RED_WOOL, ChatColor.RED + "Русский", "cmd:vip language ru");
        setButton(inventory, 14, Material.POLISHED_DIORITE, ChatColor.WHITE + "Polski", "cmd:vip language pl");
        setButton(inventory, 15, Material.BLACK_WOOL, ChatColor.GRAY + "Deutsch", "cmd:vip language de");
        setBack(inventory, 22, player);
        player.openInventory(inventory);
    }

    private void openAddFriendMenu(Player player) {
        VipMenuHolder holder = new VipMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, tr(player, "menu.add-friend-title"));
        holder.setInventory(inventory);
        fill(inventory);

        setButton(inventory, 11, Material.GRASS_BLOCK, ChatColor.GREEN + "Java", "anvil:add_friend_java",
                tr(player, "menu.add-friend-java-lore"));
        setButton(inventory, 15, Material.COBBLED_DEEPSLATE, ChatColor.AQUA + "Bedrock", "anvil:add_friend_bedrock",
                tr(player, "menu.add-friend-bedrock-lore"));
        setBack(inventory, 22, player);
        player.openInventory(inventory);
    }

    private void addNamedColors(Inventory inventory, String commandPrefix) {
        addNamedColors(inventory, commandPrefix, "");
    }

    private void addNamedColors(Inventory inventory, String commandPrefix, String suffix) {
        for (int i = 0; i < NAMED_COLORS.length && i < COLOR_SLOTS.length; i++) {
            int slot = COLOR_SLOTS[i];
            String color = NAMED_COLORS[i];
            ChatColor chatColor = colorForName(color);
            setButton(inventory, slot, materialForColor(color), chatColor + color, "cmd:" + commandPrefix + color + suffix);
        }
    }

    private Material materialForColor(String color) {
        return switch (color) {
            case "black" -> Material.BLACK_DYE;
            case "dark_blue", "blue" -> Material.BLUE_DYE;
            case "dark_green" -> Material.GREEN_DYE;
            case "dark_aqua", "aqua" -> Material.CYAN_DYE;
            case "dark_red", "red" -> Material.RED_DYE;
            case "dark_purple" -> Material.PURPLE_DYE;
            case "gold" -> Material.ORANGE_DYE;
            case "gray" -> Material.LIGHT_GRAY_DYE;
            case "dark_gray" -> Material.GRAY_DYE;
            case "green" -> Material.LIME_DYE;
            case "light_purple" -> Material.MAGENTA_DYE;
            case "yellow" -> Material.YELLOW_DYE;
            default -> Material.WHITE_DYE;
        };
    }

    private Material woolForColor(String color) {
        Material material = Material.matchMaterial(color.toUpperCase(Locale.ROOT) + "_WOOL");
        return material == null ? Material.WHITE_WOOL : material;
    }

    private Material spawnEgg(PetOption pet) {
        return spawnEgg(pet.eggName(), pet.fallback());
    }

    private Material spawnEgg(String eggName, Material fallback) {
        Material material = Material.matchMaterial(eggName);
        return material == null ? fallback : material;
    }

    private ChatColor colorForName(String color) {
        String key = color.toUpperCase(Locale.ROOT).replace("GREY", "GRAY");
        try {
            ChatColor chatColor = ChatColor.valueOf(key);
            return chatColor.isColor() ? chatColor : ChatColor.WHITE;
        } catch (IllegalArgumentException ignored) {
            return ChatColor.WHITE;
        }
    }

    private void openAnvilInput(Player player, VipAnvilInput input) {
        player.closeInventory();

        if (input == VipAnvilInput.RENAME) {
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem.getType().isAir()) {
                player.sendMessage(tr(player, "rename.no-item"));
                return;
            }
        }

        openTextDialog(
                player,
                anvilTitle(player, input),
                anvilLabel(player, input),
                anvilPlaceholder(player, input),
                anvilMaxLength(input),
                false,
                anvilIcon(input),
                null,
                text -> validateAnvilText(player, input, text),
                (clickedPlayer, text) -> acceptAnvil(clickedPlayer, input, text)
        );
    }

    private String anvilTitle(Player player, VipAnvilInput input) {
        return switch (input) {
            case PREFIX -> tr(player, "dialog.prefix-title");
            case SUFFIX -> tr(player, "dialog.suffix-title");
            case RENAME -> tr(player, "dialog.rename-title");
            case CHAT_COLOR -> tr(player, "dialog.chat-color-title");
            case ADD_FRIEND_JAVA -> tr(player, "dialog.add-friend-java-title");
            case ADD_FRIEND_BEDROCK -> tr(player, "dialog.add-friend-bedrock-title");
        };
    }

    private String anvilPlaceholder(Player player, VipAnvilInput input) {
        return switch (input) {
            case PREFIX -> tr(player, "dialog.prefix-placeholder");
            case SUFFIX -> tr(player, "dialog.suffix-placeholder");
            case RENAME -> tr(player, "dialog.rename-placeholder");
            case CHAT_COLOR -> tr(player, "dialog.chat-color-placeholder");
            case ADD_FRIEND_JAVA -> tr(player, "dialog.add-friend-java-placeholder");
            case ADD_FRIEND_BEDROCK -> tr(player, "dialog.add-friend-bedrock-placeholder");
        };
    }

    private void openChatDialog(Player player, VipChatInput input) {
        player.closeInventory();

        openTextDialog(
                player,
                chatTitle(player, input),
                chatLabel(player, input),
                chatPlaceholder(input, player),
                chatMaxLength(input),
                true,
                chatIcon(input),
                chatDescription(input, player),
                text -> null,
                (clickedPlayer, text) -> acceptChatInput(clickedPlayer, input, text)
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof VipMenuHolder)) {
            if (isMenuItem(event.getCurrentItem()) || isMenuItem(event.getCursor())) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    removeDuplicatedMenuItems(player);
                    player.updateInventory();
                }
            }
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String action = actionOf(event.getCurrentItem());
        if (action == null) {
            return;
        }

        handleAction(player, action);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof VipMenuHolder
                || isMenuItem(event.getOldCursor())) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                removeDuplicatedMenuItems(player);
                player.updateInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuItemPlace(BlockPlaceEvent event) {
        if (!isMenuItem(event.getItemInHand())) {
            return;
        }

        event.setCancelled(true);
        removeDuplicatedMenuItems(event.getPlayer());
        event.getPlayer().updateInventory();
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (event.getView().getTopInventory().getHolder() instanceof VipMenuHolder) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                removeDuplicatedMenuItems(player);
                player.updateInventory();
            });
        }
    }

    private void handleAction(Player player, String action) {
        if (action.equals("close")) {
            player.closeInventory();
            return;
        }

        if (action.equals("back")) {
            openMainMenu(player);
            return;
        }

        if (action.equals("buy")) {
            player.closeInventory();
            VipMessages.sendVipMessage(plugin, player);
            return;
        }

        if (isVipPlusMenuAction(action) && !hasVipPlus(player)) {
            player.closeInventory();
            player.sendMessage(plugin.getLang().get(player, "common.only-vip-plus"));
            return;
        }

        if (action.startsWith("petmenu:")) {
            openPetVariantMenu(player, action.substring("petmenu:".length()));
            return;
        }

        if (action.startsWith("cmd:")) {
            player.closeInventory();
            player.performCommand(action.substring("cmd:".length()));
            return;
        }

        if (action.startsWith("menu:")) {
            openSubMenu(player, action.substring("menu:".length()));
            return;
        }

        if (action.startsWith("anvil:")) {
            openAnvilInput(player, anvilInputFor(action.substring("anvil:".length())));
            return;
        }

        if (action.startsWith("chat:")) {
            openChatDialog(player, chatInputFor(action.substring("chat:".length())));
        }
    }

    private boolean isVipPlusMenuAction(String action) {
        return action.equals("cmd:vip kit")
                || action.equals("anvil:rename")
                || action.equals("chat:announcement")
                || action.equals("menu:add_friend")
                || action.equals("anvil:add_friend_java")
                || action.equals("anvil:add_friend_bedrock");
    }

    private boolean hasVipPlus(Player player) {
        return player.hasPermission("vip.vip_plus");
    }

    private void openSubMenu(Player player, String menu) {
        switch (menu) {
            case "particles" -> openParticlesMenu(player);
            case "petals" -> openPetalsMenu(player);
            case "pets" -> openPetsMenu(player);
            case "name" -> openNameMenu(player);
            case "messages" -> openMessagesMenu(player);
            case "nick_color" -> openNickColorMenu(player);
            case "chat_color" -> openChatColorMenu(player);
            case "language" -> openLanguageMenu(player);
            case "add_friend" -> openAddFriendMenu(player);
            default -> openMainMenu(player);
        }
    }

    private VipAnvilInput anvilInputFor(String input) {
        return switch (input) {
            case "prefix" -> VipAnvilInput.PREFIX;
            case "suffix" -> VipAnvilInput.SUFFIX;
            case "chat_color" -> VipAnvilInput.CHAT_COLOR;
            case "add_friend_java" -> VipAnvilInput.ADD_FRIEND_JAVA;
            case "add_friend_bedrock" -> VipAnvilInput.ADD_FRIEND_BEDROCK;
            default -> VipAnvilInput.RENAME;
        };
    }

    private VipChatInput chatInputFor(String input) {
        return switch (input) {
            case "join_message" -> VipChatInput.JOIN_MESSAGE;
            case "leave_message" -> VipChatInput.LEAVE_MESSAGE;
            default -> VipChatInput.ANNOUNCEMENT;
        };
    }

    private void openTextDialog(
            Player player,
            String title,
            String label,
            String initial,
            int maxLength,
            boolean multiline,
            Material iconMaterial,
            String description,
            Function<String, String> validator,
            BiConsumer<Player, String> accept
    ) {
        TextDialogInput.Builder textBuilder = DialogInput.text(DIALOG_TEXT_KEY, legacy(label))
                .width(320)
                .labelVisible(true)
                .initial(initial)
                .maxLength(maxLength);

        if (multiline) {
            textBuilder.multiline(TextDialogInput.MultilineOptions.create(6, 120));
        }

        List<DialogBody> body = new ArrayList<>();
        if (iconMaterial != null) {
            body.add(DialogBody.item(new ItemStack(iconMaterial))
                    .showTooltip(false)
                    .showDecorations(false)
                    .width(32)
                    .height(32)
                    .build());
        }
        if (description != null && !description.isBlank()) {
            body.add(DialogBody.plainMessage(legacy(description), 320));
        }

        List<ActionButton> buttons = new java.util.ArrayList<>();

        ActionButton cancelButton = ActionButton.builder(legacy(tr(player, "dialog.cancel")))
                .width(150)
                .action(DialogAction.staticAction(ClickEvent.callback(Audience::closeDialog, callbackOptions())))
                .build();
        buttons.add(cancelButton);

        ActionButton acceptButton = ActionButton.builder(legacy(tr(player, "dialog.accept")))
                .width(150)
                .action(DialogAction.customClick(
                        (response, audience) -> handleDialogSubmit(response, audience, initial, validator, accept),
                        callbackOptions()
                ))
                .build();
        buttons.add(acceptButton);

        if (shouldShowColorButtons(label)) {
            for (DialogTag colorTag : DIALOG_COLOR_TAGS) {
                buttons.add(ActionButton.builder(colorTag.label())
                        .width(145)
                        .action(DialogAction.customClick(
                                (response, audience) -> handleColorButton(
                                        response,
                                        audience,
                                        colorTag.token(),
                                        title,
                                        label,
                                        maxLength,
                                        multiline,
                                        iconMaterial,
                                        description,
                                        validator,
                                        accept
                                ),
                                callbackOptions()
                        ))
                        .build());
            }
        }

        ActionButton closeButton = ActionButton.builder(Component.text("X", NamedTextColor.RED))
                .width(40)
                .action(DialogAction.staticAction(ClickEvent.callback(Audience::closeDialog, callbackOptions())))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(legacy(title))
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .body(body)
                        .inputs(List.of(textBuilder.build()))
                        .build())
                .type(DialogType.multiAction(buttons)
                        .exitAction(closeButton)
                        .columns(2)
                        .build()));

        player.showDialog(dialog);
    }

    private void handleColorButton(
            DialogResponseView response,
            Audience audience,
            String colorTag,
            String title,
            String label,
            int maxLength,
            boolean multiline,
            Material iconMaterial,
            String description,
            Function<String, String> validator,
            BiConsumer<Player, String> accept
    ) {
        if (!(audience instanceof Player player)) {
            return;
        }

        String current = response.getText(DIALOG_TEXT_KEY);
        if (current == null) {
            current = "";
        }

        String updated = current + colorTag;
        if (updated.length() > maxLength) {
            player.sendMessage(tr(player, "dialog.too-long"));
            return;
        }

        String finalUpdated = updated;
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            openTextDialog(
                    player,
                    title,
                    label,
                    finalUpdated,
                    maxLength,
                    multiline,
                    iconMaterial,
                    description,
                    validator,
                    accept
            );
        });
    }

    private boolean shouldShowColorButtons(String label) {
        return !label.toLowerCase(Locale.ROOT).contains("java")
                && !label.toLowerCase(Locale.ROOT).contains("bedrock");
    }

    private void handleDialogSubmit(
            DialogResponseView response,
            Audience audience,
            String initial,
            Function<String, String> validator,
            BiConsumer<Player, String> accept
    ) {
        if (!(audience instanceof Player player)) {
            return;
        }

        String text = response.getText(DIALOG_TEXT_KEY);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            String error = validateDialogText(player, text, initial, validator);
            if (error != null) {
                player.sendMessage(error);
                return;
            }

            player.closeDialog();
            accept.accept(player, text);
        });
    }

    private String validateDialogText(Player player, String text, String initial, Function<String, String> validator) {
        if (text == null || text.isBlank()) {
            return tr(player, "dialog.empty");
        }

        return validator == null ? null : validator.apply(text);
    }

    private ClickCallback.Options callbackOptions() {
        return ClickCallback.Options.builder()
                .uses(10)
                .lifetime(Duration.ofMinutes(10))
                .build();
    }

    private Material anvilIcon(VipAnvilInput input) {
        return switch (input) {
            case PREFIX, SUFFIX -> Material.PAPER;
            case RENAME -> Material.ANVIL;
            case CHAT_COLOR -> Material.LIME_DYE;
            case ADD_FRIEND_JAVA -> Material.GRASS_BLOCK;
            case ADD_FRIEND_BEDROCK -> Material.COBBLED_DEEPSLATE;
        };
    }

    private Material chatIcon(VipChatInput input) {
        return switch (input) {
            case JOIN_MESSAGE -> Material.OAK_SIGN;
            case LEAVE_MESSAGE -> Material.SPRUCE_SIGN;
            case ANNOUNCEMENT -> Material.BELL;
        };
    }

    private String anvilLabel(Player player, VipAnvilInput input) {
        return switch (input) {
            case PREFIX -> tr(player, "dialog.prefix-label");
            case SUFFIX -> tr(player, "dialog.suffix-label");
            case RENAME -> tr(player, "dialog.rename-label");
            case CHAT_COLOR -> tr(player, "dialog.chat-color-label");
            case ADD_FRIEND_JAVA -> tr(player, "dialog.add-friend-java-label");
            case ADD_FRIEND_BEDROCK -> tr(player, "dialog.add-friend-bedrock-label");
        };
    }

    private int anvilMaxLength(VipAnvilInput input) {
        return switch (input) {
            case PREFIX, SUFFIX -> 80;
            case RENAME -> 140;
            case CHAT_COLOR -> 24;
            case ADD_FRIEND_JAVA, ADD_FRIEND_BEDROCK -> 16;
        };
    }

    private String chatTitle(Player player, VipChatInput input) {
        return switch (input) {
            case JOIN_MESSAGE -> tr(player, "dialog.join-title");
            case LEAVE_MESSAGE -> tr(player, "dialog.leave-title");
            case ANNOUNCEMENT -> tr(player, "dialog.announcement-title");
        };
    }

    private String chatLabel(Player player, VipChatInput input) {
        return switch (input) {
            case JOIN_MESSAGE -> tr(player, "dialog.join-label");
            case LEAVE_MESSAGE -> tr(player, "dialog.leave-label");
            case ANNOUNCEMENT -> tr(player, "dialog.announcement-label");
        };
    }

    private String chatPlaceholder(VipChatInput input, Player player) {
        return switch (input) {
            case JOIN_MESSAGE -> tr(player, "dialog.join-placeholder", "%player%", player.getName());
            case LEAVE_MESSAGE -> tr(player, "dialog.leave-placeholder", "%player%", player.getName());
            case ANNOUNCEMENT -> tr(player, "dialog.announcement-placeholder");
        };
    }

    private int chatMaxLength(VipChatInput input) {
        return switch (input) {
            case JOIN_MESSAGE, LEAVE_MESSAGE -> 256;
            case ANNOUNCEMENT -> 300;
        };
    }

    private String chatDescription(VipChatInput input, Player player) {
        return switch (input) {
            case JOIN_MESSAGE, LEAVE_MESSAGE ->
                    tr(player, "dialog.message-description", "%player%", player.getName());
            case ANNOUNCEMENT ->
                    tr(player, "dialog.announcement-description");
        };
    }

    private String validateAnvilText(Player player, VipAnvilInput input, String text) {
        if (text == null || text.isBlank()) {
            return tr(player, "dialog.empty");
        }

        if (input == VipAnvilInput.RENAME
                && ChatColor.stripColor(ColorTokens.translateHashColors(text)).trim().length() > 50) {
            return tr(player, "rename.too-long");
        }

        return null;
    }

    private void acceptAnvil(Player player, VipAnvilInput input, String text) {
        if (!player.isOnline()) {
            return;
        }

        String command = switch (input) {
            case PREFIX -> "vip prefix " + text;
            case SUFFIX -> "vip suffix " + text;
            case CHAT_COLOR -> "vip chatcolor " + text;
            case ADD_FRIEND_JAVA -> "vip addFriend java " + text;
            case ADD_FRIEND_BEDROCK -> "vip addFriend bedrock " + text;
            case RENAME -> "vip rename " + text;
        };

        player.performCommand(command);
    }

    private void acceptChatInput(Player player, VipChatInput input, String message) {
        if (!player.isOnline()) {
            return;
        }

        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("скасувати")) {
            player.sendMessage(tr(player, "dialog.input-cancelled"));
            return;
        }

        String command = switch (input) {
            case JOIN_MESSAGE -> "vip join_message " + message;
            case LEAVE_MESSAGE -> "vip leave_message " + message;
            case ANNOUNCEMENT -> "vip announcement " + message;
        };

        player.performCommand(command);
    }

    private void setBack(Inventory inventory, int slot, Player player) {
        setButton(inventory, slot, Material.ARROW, tr(player, "menu.back"), "back");
    }

    private void setButton(Inventory inventory, int slot, Material material, String name, String action, String... lore) {
        inventory.setItem(slot, menuItem(material, name, action, lore));
    }

    private void fill(Inventory inventory) {
        ItemStack filler = menuItem(Material.GRAY_STAINED_GLASS_PANE, " ", "filler");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    private ItemStack menuItem(Material material, String name, String action, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(List.of(lore));
            }
            meta.getPersistentDataContainer().set(menuActionKey, PersistentDataType.STRING, action);
            item.setItemMeta(meta);
        }

        return item;
    }

    private String actionOf(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(menuActionKey, PersistentDataType.STRING);
    }

    private boolean isMenuItem(ItemStack item) {
        return actionOf(item) != null;
    }

    public boolean removeDuplicatedMenuItems(Player player) {
        boolean removed = false;
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (isMenuItem(item)) {
                player.getInventory().setItem(slot, null);
                removed = true;
            }
        }

        if (isMenuItem(player.getItemOnCursor())) {
            player.setItemOnCursor(null);
            removed = true;
        }

        return removed;
    }
}
