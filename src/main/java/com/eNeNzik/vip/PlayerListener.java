package com.eNeNzik.vip;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

import java.net.URI;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final Vip plugin;
    private final ParticleManager particleManager;
    private final NickColorManager nickColorManager;
    private final VipCommand vipCommand;
    public PlayerListener(Vip plugin, ParticleManager particleManager, NickColorManager nickColorManager, VipCommand vipCommand) {
        this.plugin = plugin;
        this.particleManager = particleManager;
        this.nickColorManager = nickColorManager;
        this.vipCommand = vipCommand;
    }

    @EventHandler
    public void onPlayerLinksSend(PlayerLinksSendEvent event) {
        event.getLinks().addLink(
                net.kyori.adventure.text.Component.text("Minezik", net.kyori.adventure.text.format.NamedTextColor.GREEN),
                URI.create("https://minezik.com.ua/")
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer == null || !onlinePlayer.isOnline()) {
                return;
            }
            plugin.sendVipPromotion(onlinePlayer);
        }, 20L*120L);
        NamespacedKey lightRecipeKey = new NamespacedKey(plugin, "vip_light");
        if (player.hasPermission("vip.vip_plus")) {
            player.discoverRecipe(lightRecipeKey);
        } else {
            player.undiscoverRecipe(lightRecipeKey);
        }

        if (!hasVip(player)) {
            nickColorManager.removeSavedNickStyle(player);
            event.setJoinMessage(defaultJoinMessage(player, false));
            return;}

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            String petName = vipCommand.petsName.get(player.getUniqueId());
            if (petName != null) {
                vipCommand.createPet(petName, player, false);
            }
        }, 20L);
        particleManager.restoreParticles(event.getPlayer());
        // Keep nametags consistent for everyone
        nickColorManager.applySavedNickStyleForViewer(event.getPlayer());
        nickColorManager.applySavedNickStyle(event.getPlayer());
        String path = "players." + event.getPlayer().getUniqueId() + ".join-message";
        String template = plugin.getPlayerData().getString(path);
        if (template != null && !template.isBlank()) {
            event.setJoinMessage(template);
        } else {
            event.setJoinMessage(defaultJoinMessage(player, true));
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKitPreviewClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof KitPreviewHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            if (event.getRawSlot() == VipCommand.VIP_PLUS_KIT_CLAIM_SLOT) {
                vipCommand.claimVipPlusKit(player);
                return;
            }

            if (event.getRawSlot() == VipCommand.VIP_PLUS_KIT_CANCEL_SLOT) {
                player.closeInventory();
                return;
            }

            refreshInventory(player);
            return;
        }

        if (hasTaggedPreviewItem(event) && event.getWhoClicked() instanceof Player player) {
            event.setCancelled(true);
            refreshInventory(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKitPreviewDrag(InventoryDragEvent event) {
        if (vipCommand.hasTaggedKitPreviewItem(event.getOldCursor())) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                refreshInventory(player);
            }
            return;
        }

        if (event.getView().getTopInventory().getHolder() instanceof KitPreviewHolder) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                refreshInventory(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKitPreviewPlace(BlockPlaceEvent event) {
        if (!vipCommand.hasTaggedKitPreviewItem(event.getItemInHand())) {
            return;
        }

        event.setCancelled(true);
        refreshInventory(event.getPlayer());
    }

    @EventHandler
    public void onKitPreviewClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof KitPreviewHolder && event.getPlayer() instanceof Player player) {
            refreshInventory(player);
        }
    }

    private void refreshInventory(Player player) {
        vipCommand.removeDuplicatedKitPreviewItems(player);
        player.updateInventory();
        Bukkit.getScheduler().runTask(plugin, () -> {
            vipCommand.removeDuplicatedKitPreviewItems(player);
            player.updateInventory();
        });
    }

    private boolean hasTaggedPreviewItem(InventoryClickEvent event) {
        if (vipCommand.hasTaggedKitPreviewItem(event.getCurrentItem())
                || vipCommand.hasTaggedKitPreviewItem(event.getCursor())) {
            return true;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return false;
        }

        int hotbarButton = event.getHotbarButton();
        return hotbarButton >= 0
                && hotbarButton <= 8
                && vipCommand.hasTaggedKitPreviewItem(player.getInventory().getItem(hotbarButton));
    }

    @EventHandler
    public void onPrepareVipLightCraft(PrepareItemCraftEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) {
            return;
        }

        if (!isVipLightRecipe(event.getRecipe())) {
            return;
        }

        if (!player.hasPermission("vip.vip_plus")) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onVipLightCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!isVipLightRecipe(event.getRecipe())) {
            return;
        }

        if (!player.hasPermission("vip.vip_plus")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getLang().get(player, "craft.vip-light-only"));
        }
    }

    private boolean isVipLightRecipe(Recipe recipe) {
        if (!(recipe instanceof Keyed keyed)) {
            return false;
        }

        return keyed.getKey().equals(new NamespacedKey(plugin, "vip_light"));
    }

    @EventHandler
    public void onHatClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if(!(event.getWhoClicked().hasPermission("vip.vip")||event.getWhoClicked().hasPermission("vip.vip_plus"))) return;
        if (event.getSlotType() != InventoryType.SlotType.ARMOR) {
            return;
        }

        if (event.getRawSlot() != 5) {
            return;
        }

        ItemStack cursor = event.getCursor();

        if (cursor == null || cursor.getType().isAir()) {
            return;
        }
        event.setCancelled(true);

        ItemStack oldHelmet = player.getInventory().getHelmet();

        player.getInventory().setHelmet(cursor.clone());
        event.setCursor(oldHelmet);

        player.updateInventory();
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        vipCommand.removePet(player);
        particleManager.stopParticles(player, false, false);

        if (!hasVip(player)) {
            nickColorManager.removeSavedNickStyle(player);
            event.setQuitMessage(defaultQuitMessage(player, false));
            return;
        }

        String path = "players." + player.getUniqueId() + ".leave-message";
        String template = plugin.getPlayerData().getString(path);
        if (template != null && !template.isBlank()) {
            event.setQuitMessage(template);
        } else {
            event.setQuitMessage(defaultQuitMessage(player, true));
        }

        nickColorManager.removeSavedNickStyle(player);
    }
    @EventHandler
    public void onDispenserShearPet(BlockShearEntityEvent event) {
        Entity entity = event.getEntity();

        if (!vipCommand.pets.containsValue(entity)) {
            return;
        }

        event.setCancelled(true);
    }
    @EventHandler
    public void onPetInteract(PlayerInteractEntityEvent event) {
        if (!vipCommand.pets.containsValue(event.getRightClicked())) {
            return;
        }

        event.setCancelled(true);
    }
    @EventHandler
    public void onPetShear(PlayerShearEntityEvent event) {
        if (!vipCommand.pets.containsValue(event.getEntity())) {
            return;
        }
        event.setCancelled(true);
    }
    @EventHandler
    public void onPetLeash(PlayerLeashEntityEvent event) {
        if (vipCommand.pets.containsValue(event.getEntity())) {
            event.setCancelled(true);
            event.getPlayer().spigot().sendMessage(
                    net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(plugin.getLang().get(event.getPlayer(), "pet.leash-denied"))
            );
        }
    }
    @EventHandler
    public void onPetDamage(EntityDamageByEntityEvent event) {
        if (!vipCommand.pets.containsValue(event.getEntity())) {
            return;
        }
        event.setCancelled(true);
    }
    @EventHandler
    public void onPetCreeperPower(CreeperPowerEvent event) {
        if (!vipCommand.pets.containsValue(event.getEntity())) {
            return;
        }
        if (event.getCause() == CreeperPowerEvent.PowerCause.LIGHTNING) {
            event.setCancelled(true);
            event.getEntity().setPowered(false);
        }
    }
    @EventHandler
    public void onPetPigZap(PigZapEvent event) {
        if (!vipCommand.pets.containsValue(event.getEntity())) {
            return;
        }
        event.setCancelled(true);
        event.getPigZombie().remove();
        event.getEntity().setFireTicks(0);
        Player owner = null;
        for (java.util.UUID uuid : vipCommand.pets.keySet()) {
            if (vipCommand.pets.get(uuid).equals(event.getEntity())) {
                owner = Bukkit.getPlayer(uuid);
                break;
            }
        }
        if (owner == null || !owner.isOnline()) {
            return;
        }
        Player finalOwner = owner;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!finalOwner.isOnline()) {
                return;
            }
            Entity pet = vipCommand.pets.get(finalOwner.getUniqueId());
            if (pet == null || pet.isDead() || !pet.isValid()) {
                vipCommand.createPet("свиня", finalOwner, false);
                return;
            }
            pet.setFireTicks(0);
            if (!finalOwner.getPassengers().contains(pet)) {
                finalOwner.addPassenger(pet);
            }
        }, 1L);
    }
    @EventHandler
    public void onPetLightningTransform(EntityTransformEvent event) {
        if (!vipCommand.pets.containsValue(event.getEntity())) {
            return;
        }
        if (event.getTransformReason() == EntityTransformEvent.TransformReason.LIGHTNING) {
            event.setCancelled(true);
            event.getEntity().setFireTicks(0);
        }
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        vipCommand.removePet(event.getEntity());
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if(!event.getPlayer().hasPermission("vip.vip")&&!event.getPlayer().hasPermission("vip.vip_plus")) return;
        Player player = event.getPlayer();
        String petName = vipCommand.petsName.get(player.getUniqueId());
        if (petName == null) {
            return;
        }
        vipCommand.removePet(player);
        vipCommand.createPet(petName, player, false);
    }
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if(!event.getPlayer().hasPermission("vip.vip")&&!event.getPlayer().hasPermission("vip.vip_plus")) return;
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            String petName = vipCommand.petsName.get(player.getUniqueId());
            if (petName != null) {
                vipCommand.createPet(petName, player, false);
            }
        }, 1L);
    }

    private boolean hasVip(Player player) {
        return player.hasPermission("vip.vip") || player.hasPermission("vip.vip_plus");
    }

    private String defaultJoinMessage(Player player, boolean useStyledName) {
        return ChatColor.YELLOW + displayNameForMessage(player, useStyledName) + ChatColor.YELLOW + " joined the game";
    }

    private String defaultQuitMessage(Player player, boolean useStyledName) {
        return ChatColor.YELLOW + displayNameForMessage(player, useStyledName) + ChatColor.YELLOW + " left the game";
    }

    private String displayNameForMessage(Player player, boolean useStyledName) {
        if (!useStyledName || !hasSavedNickStyle(player)) {
            return player.getName();
        }

        String prefix = nickColorManager.getSavedPrefix(player.getUniqueId());
        String suffix = nickColorManager.getSavedSuffix(player.getUniqueId());
        ChatColor color = nickColorManager.getSavedColor(player.getUniqueId());

        return (prefix != null ? prefix : "")
                + (color != null ? color : ChatColor.WHITE)
                + player.getName()
                + ChatColor.RESET
                + (suffix != null ? suffix : "");
    }

    private boolean hasSavedNickStyle(Player player) {
        return nickColorManager.getSavedColor(player.getUniqueId()) != null
                || nickColorManager.getSavedPrefix(player.getUniqueId()) != null
                || nickColorManager.getSavedSuffix(player.getUniqueId()) != null;
    }
}
