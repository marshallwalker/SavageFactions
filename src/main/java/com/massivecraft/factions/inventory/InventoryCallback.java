package com.massivecraft.factions.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class InventoryCallback implements InventoryEventConsumer, InventoryHolder {

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
    }

    @Override
    public void onInventoryOpen(InventoryOpenEvent event) {
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
