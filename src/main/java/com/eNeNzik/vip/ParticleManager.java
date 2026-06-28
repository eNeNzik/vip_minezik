package com.eNeNzik.vip;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ParticleManager {

    private final JavaPlugin plugin;
    private final Map<UUID, BukkitTask> particleTasks = new HashMap<>();
    private final Map<UUID, ParticleStyle> activeStyles = new HashMap<>();
    private final Map<UUID, ParticleStyle> savedStyles = new HashMap<>();
    private final Map<UUID, Integer> petalsLevels = new HashMap<>();

    public ParticleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadSavedStyles();
    }

    private String lang(Player player, String path, String... replacements) {
        if (plugin instanceof Vip vipPlugin) {
            return vipPlugin.getLang().get(player, path, replacements);
        }

        return path;
    }

    public void shutdown() {
        for (BukkitTask task : particleTasks.values()) {
            task.cancel();
        }
        particleTasks.clear();
        activeStyles.clear();
    }

    public void startParticleStyle(Player player, ParticleStyle style, Boolean showMessage) {
        stopParticles(player, false, false);

        UUID uuid = player.getUniqueId();
        BukkitTask task;

        switch (style) {
            case RGB:
                task = createRgbTask(player, uuid);
                if(showMessage)player.sendMessage(lang(player, "particles.enabled", "%style%", "RGB"));
                break;
            case END_ROD:
                task = createEndRodTask(player, uuid);
                if(showMessage)player.sendMessage(lang(player, "particles.enabled", "%style%", "END_ROD"));
                break;
            case HEART:
                task = createHeartTask(player, uuid);
                if(showMessage)player.sendMessage(lang(player, "particles.enabled", "%style%", "HEART"));
                break;
            case FIRE:
                task = createFireTask(player, uuid);
                if(showMessage)player.sendMessage(lang(player, "particles.enabled", "%style%", "FIRE"));
                break;
            case GLOWING:
                task = createGlowingTask(player, uuid);
                if(showMessage)player.sendMessage(lang(player, "particles.enabled", "%style%", "GLOWING"));
                break;
            case PETALS:
                task = createPetalsTask(player, uuid, 5);
                petalsLevels.put(uuid, 5);
                if(showMessage)player.sendMessage(lang(player, "particles.petals-enabled", "%level%", "5"));
                break;
            default:
                return;
        }

        particleTasks.put(uuid, task);
        activeStyles.put(uuid, style);
        savedStyles.put(uuid, style);

        if (style != ParticleStyle.PETALS) {
            petalsLevels.remove(uuid);
        }
    }

    public void startPetals(Player player, int petalsLevel, boolean showMessage) {
        stopParticles(player, false, false);

        UUID uuid = player.getUniqueId();
        BukkitTask task = createPetalsTask(player, uuid, petalsLevel);

        particleTasks.put(uuid, task);
        activeStyles.put(uuid, ParticleStyle.PETALS);
        savedStyles.put(uuid, ParticleStyle.PETALS);
        petalsLevels.put(uuid, petalsLevel);
        if(showMessage) player.sendMessage(lang(player, "particles.petals-enabled", "%level%", String.valueOf(petalsLevel)));
    }

    public void restoreParticles(Player player) {
        UUID uuid = player.getUniqueId();
        ParticleStyle style = savedStyles.get(uuid);

        if (style == null) {
            return;
        }

        if (style == ParticleStyle.PETALS) {
            int level = petalsLevels.getOrDefault(uuid, 5);
            startPetals(player, level, false);
        } else {
            startParticleStyle(player, style, false);
        }
    }

    public void stopParticles(Player player, boolean sendMessage, boolean removeSavedStyle) {
        UUID uuid = player.getUniqueId();

        BukkitTask task = particleTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }

        activeStyles.remove(uuid);

        if (removeSavedStyle) {
            savedStyles.remove(uuid);
            petalsLevels.remove(uuid);
        }

        if (sendMessage) {
            player.sendMessage(lang(player, "particles.disabled"));
        }
    }
    private void loadSavedStyles() {
        if (!(plugin instanceof Vip vipPlugin)) {
            return;
        }

        ConfigurationSection playersSection = vipPlugin.getPlayerData().getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }

        for (String uuidString : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                String styleName = playersSection.getString(uuidString + ".style");

                if (styleName == null) {
                    continue;
                }

                ParticleStyle style = ParticleStyle.valueOf(styleName);
                savedStyles.put(uuid, style);

                if (style == ParticleStyle.PETALS) {
                    petalsLevels.put(uuid, playersSection.getInt(uuidString + ".petals-level", 5));
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void saveAllStyles() {
        if (!(plugin instanceof Vip vipPlugin)) {
            return;
        }

        // Clear only particle-related keys, so we don't delete other saved player data (e.g. join-message)
        ConfigurationSection playersSection = vipPlugin.getPlayerData().getConfigurationSection("players");
        if (playersSection != null) {
            for (String uuidString : playersSection.getKeys(false)) {
                String base = "players." + uuidString;
                vipPlugin.getPlayerData().set(base + ".style", null);
                vipPlugin.getPlayerData().set(base + ".petals-level", null);
            }
        }

        for (Map.Entry<UUID, ParticleStyle> entry : savedStyles.entrySet()) {
            UUID uuid = entry.getKey();
            ParticleStyle style = entry.getValue();

            String path = "players." + uuid;
            vipPlugin.getPlayerData().set(path + ".style", style.name());
            vipPlugin.getPlayerData().set(path + ".petals-level", petalsLevels.getOrDefault(uuid, 0));
        }

        vipPlugin.savePlayerData();
    }
    private BukkitTask createRgbTask(Player player, UUID uuid) {
        double animationSpeed = plugin.getConfig().getDouble("particles.rgb.animation-speed", 0.020);
        int ringPoints = plugin.getConfig().getInt("particles.rgb.ring-points", 28);
        int updateTicks = plugin.getConfig().getInt("particles.rgb.update-ticks", 1);
        int trailRings = plugin.getConfig().getInt("particles.rgb.trail-rings", 3);
        double ringSpacing = plugin.getConfig().getDouble("particles.rgb.ring-spacing", 0.32);
        double topRadius = plugin.getConfig().getDouble("particles.rgb.top-radius", 0.35);
        double bottomRadius = plugin.getConfig().getDouble("particles.rgb.bottom-radius", 0.95);
        double totalHeight = plugin.getConfig().getDouble("particles.rgb.total-height", 2.0);
        double totalSize = plugin.getConfig().getDouble("particles.rgb.size", 1);

        return new BukkitRunnable() {
            double time = 0.0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    particleTasks.remove(uuid);
                    activeStyles.remove(uuid);
                    cancel();
                    return;
                }

                Location base = player.getLocation().clone().add(0, 0.2, 0);

                for (int trail = 0; trail < trailRings; trail++) {
                    double progress = ((time - trail * ringSpacing) % 1.0 + 1.0) % 1.0;

                    double y = totalHeight * (1.0 - progress);
                    double radius = topRadius + (bottomRadius - topRadius) * progress;
                    float size = (float)(totalSize - (trail * 0.18f));
                    if (size < 0.5f) {
                        size = 0.5f;
                    }

                    for (int i = 0; i < ringPoints; i++) {
                        double circleProgress = (double) i / ringPoints;
                        double angle = circleProgress * Math.PI * 2 + time * 4.0;

                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location loc = base.clone().add(x, y, z);

                        float hue = (float) ((circleProgress + time * 0.15 - trail * 0.08) % 1.0);
                        if (hue < 0) {
                            hue += 1.0f;
                        }

                        Color color = hsvToBukkitColor(hue, 1.0f, 1.0f);

                        player.getWorld().spawnParticle(
                                Particle.DUST,
                                loc,
                                1,
                                new Particle.DustOptions(color, size)
                        );
                    }
                }

                time += animationSpeed;
            }
        }.runTaskTimer(plugin, 0L, updateTicks);
    }

    private BukkitTask createEndRodTask(Player player, UUID uuid) {
        return new BukkitRunnable() {
            double angle = 0.0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    particleTasks.remove(uuid);
                    activeStyles.remove(uuid);
                    cancel();
                    return;
                }

                Location center = player.getLocation().clone().add(0, 1.2, 0);
                double radius = 1.0;

                for (int i = 0; i < 12; i++) {
                    double currentAngle = angle + (2 * Math.PI / 12) * i;
                    double x = Math.cos(currentAngle) * radius;
                    double z = Math.sin(currentAngle) * radius;
                    double y = Math.sin(angle * 2 + i * 0.4) * 0.15;

                    Location loc = center.clone().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                }

                angle += 0.12;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private BukkitTask createHeartTask(Player player, UUID uuid) {
        return new BukkitRunnable() {
            double angle = 0.0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    particleTasks.remove(uuid);
                    activeStyles.remove(uuid);
                    cancel();
                    return;
                }

                Location center = player.getLocation().clone().add(0, 2.1, 0);
                double radius = 0.55;

                for (int i = 0; i < 6; i++) {
                    double currentAngle = angle + (2 * Math.PI / 6) * i;
                    double x = Math.cos(currentAngle) * radius;
                    double z = Math.sin(currentAngle) * radius;
                    double y = Math.sin(angle * 2 + i) * 0.08;

                    Location heartLoc = center.clone().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.HEART, heartLoc, 1, 0, 0, 0, 0);
                }

                angle += 0.18;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    private BukkitTask createFireTask(Player player, UUID uuid) {
        return new BukkitRunnable() {
            double time = 0.50;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    particleTasks.remove(uuid);
                    activeStyles.remove(uuid);
                    cancel();
                    return;
                }

                Location center = player.getLocation().clone().add(0, 1.0, 0);

                double orbitRadius = 0.75;
                double orbitAngle = time * 1.65;

                double objectX = Math.cos(orbitAngle) * orbitRadius;
                double objectZ = Math.sin(orbitAngle) * orbitRadius;
                double objectY = 0.25 + Math.sin(time * 1.7) * 0.10;

                Location fireCore = center.clone().add(objectX, objectY, objectZ);

                for (int i = 0; i < 10; i++) {
                    double part = (Math.PI * 2 / 10) * i + time * 2.4;
                    double x = Math.cos(part) * 0.18;
                    double z = Math.sin(part) * 0.18;
                    double y = Math.sin(time * 2.3 + i * 0.7) * 0.08;

                    Location flameLoc = fireCore.clone().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.FLAME, flameLoc, 1, 0, 0, 0, 0);
                }

                for (int i = 0; i < 6; i++) {
                    double part = (Math.PI * 2 / 6) * i - time * 1.9;
                    double x = Math.cos(part) * 0.10;
                    double z = Math.sin(part) * 0.10;
                    double y = 0.05 + Math.sin(time * 2.0 + i) * 0.04;

                    Location smallFlame = fireCore.clone().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.SMALL_FLAME, smallFlame, 1, 0, 0, 0, 0);
                }

                player.getWorld().spawnParticle(
                        Particle.SMOKE,
                        fireCore.clone().add(0, 0.10, 0),
                        1,
                        0.05, 0.06, 0.05, 0.0
                );

                Location boots = player.getLocation().clone().add(0, 0.08, 0);
                player.getWorld().spawnParticle(
                        Particle.SOUL_FIRE_FLAME,
                        boots,
                        1,
                        0.10, 0.02, 0.10, 0.0
                );

                time += 0.040;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private BukkitTask createGlowingTask(Player player, UUID uuid) {
        return new BukkitRunnable() {
            double angle = 0.0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    particleTasks.remove(uuid);
                    activeStyles.remove(uuid);
                    cancel();
                    return;
                }

                Location center = player.getLocation().clone().add(0, 1.15, 0);

                for (int i = 0; i < 8; i++) {
                    double currentAngle = angle + (Math.PI * 2 / 8) * i;
                    double radius = 0.65 + Math.sin(angle * 1.6 + i) * 0.12;
                    double x = Math.cos(currentAngle) * radius;
                    double z = Math.sin(currentAngle) * radius;
                    double y = Math.sin(angle * 2.0 + i * 0.6) * 0.55;

                    Location loc = center.clone().add(x, y, z);
                    player.getWorld().spawnParticle(Particle.GLOW, loc, 1, 0.03, 0.03, 0.03, 0.0);
                }

                if (((int) (angle * 10)) % 6 == 0) {
                    player.getWorld().spawnParticle(
                            Particle.GLOW_SQUID_INK,
                            center.clone().add(0, 0.15, 0),
                            1,
                            0.25, 0.45, 0.25, 0.0
                    );
                }

                angle += 0.18;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private BukkitTask createPetalsTask(Player player, UUID uuid, int petalsLevel) {
        return new BukkitRunnable() {
            final Random random = new Random();

            @Override
            public void run() {
                if (!player.isOnline()) {
                    particleTasks.remove(uuid);
                    activeStyles.remove(uuid);
                    cancel();
                    return;
                }

                Location base = player.getLocation().clone().add(0, 1.0, 0);
                int leafCount = petalsLevel;

                for (int i = 0; i < leafCount; i++) {
                    double x = (random.nextDouble() - 0.5) * 1.6;
                    double y = -0.7 + random.nextDouble() * 2.4;
                    double z = (random.nextDouble() - 0.5) * 1.6;

                    Location loc = base.clone().add(x, y, z);

                    if (random.nextInt(5) == 0) {
                        player.getWorld().spawnParticle(
                                Particle.CHERRY_LEAVES,
                                loc,
                                1,
                                0.02, 0.02, 0.02, 0.0
                        );
                    } else {
                        Color color = randomLeafColor(random);

                        player.getWorld().spawnParticle(
                                Particle.TINTED_LEAVES,
                                loc,
                                1,
                                0.0, 0.0, 0.0, 0.0,
                                color
                        );
                    }
                }

                if (random.nextInt(8) == 0) {
                    double sporeX = (random.nextDouble() - 0.5) * 2.6;
                    double sporeY = -0.7 + random.nextDouble() * 2.4;
                    double sporeZ = (random.nextDouble() - 0.5) * 2.6;

                    Location sporeLoc = base.clone().add(sporeX, sporeY, sporeZ);

                    player.getWorld().spawnParticle(
                            Particle.SPORE_BLOSSOM_AIR,
                            sporeLoc,
                            1,
                            0.03, 0.03, 0.03, 0.0
                    );
                }
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    private Color randomLeafColor(Random random) {
        return lerpColor(
                Color.fromRGB(25, 110, 35),
                Color.fromRGB(215, 255, 85),
                random.nextFloat()
        );
    }

    private Color lerpColor(Color from, Color to, float t) {
        int red = (int) (from.getRed() + (to.getRed() - from.getRed()) * t);
        int green = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * t);
        int blue = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * t);
        return Color.fromRGB(red, green, blue);
    }

    private Color hsvToBukkitColor(float hue, float saturation, float brightness) {
        int rgb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return Color.fromRGB(red, green, blue);
    }
}
