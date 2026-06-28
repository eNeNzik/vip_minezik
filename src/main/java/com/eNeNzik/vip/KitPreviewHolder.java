package com.eNeNzik.vip;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class KitPreviewHolder implements InventoryHolder {

    private Inventory inventory;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
