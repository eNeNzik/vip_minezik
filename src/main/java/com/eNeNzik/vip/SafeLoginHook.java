package com.eNeNzik.vip;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class SafeLoginHook {

    private final Vip plugin;
    private Plugin safeLoginPlugin;
    private Method isPlayerLoggedInMethod;
    private boolean warnedAboutMissingApi;
    private boolean warnedAboutError;

    SafeLoginHook(Vip plugin) {
        this.plugin = plugin;
    }

    boolean shouldBlockChat(Player player) {
        if (player == null || !hookAvailable()) {
            return false;
        }

        try {
            Object result = isPlayerLoggedInMethod.invoke(safeLoginPlugin, player);
            return result instanceof Boolean loggedIn && !loggedIn;
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException exception) {
            clearHook();
            warnAboutError(exception);
            return false;
        }
    }

    private boolean hookAvailable() {
        if (safeLoginPlugin != null && safeLoginPlugin.isEnabled() && isPlayerLoggedInMethod != null) {
            return true;
        }

        clearHook();
        Plugin foundPlugin = Bukkit.getPluginManager().getPlugin("SafeLogin");
        if (foundPlugin == null || !foundPlugin.isEnabled()) {
            return false;
        }

        try {
            Method method = foundPlugin.getClass().getMethod("isPlayerLoggedIn", Player.class);
            safeLoginPlugin = foundPlugin;
            isPlayerLoggedInMethod = method;
            return true;
        } catch (NoSuchMethodException exception) {
            if (!warnedAboutMissingApi) {
                plugin.getLogger().warning("SafeLogin is installed, but it does not expose isPlayerLoggedIn(Player). Chat integration is disabled.");
                warnedAboutMissingApi = true;
            }
            return false;
        }
    }

    private void clearHook() {
        safeLoginPlugin = null;
        isPlayerLoggedInMethod = null;
    }

    private void warnAboutError(Exception exception) {
        if (warnedAboutError) {
            return;
        }

        plugin.getLogger().warning("SafeLogin chat check failed. Chat integration is disabled until the hook reconnects: "
                + exception.getMessage());
        warnedAboutError = true;
    }
}
