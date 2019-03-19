package com.massivecraft.factions.listeners;

import com.massivecraft.factions.inventory.InventoryCallback;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    private void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();

        if (inventory == null) {
            return;
        }

        InventoryHolder inventoryHolder = inventory.getHolder();

        if (!(inventoryHolder instanceof InventoryCallback)) {
            InventoryHolder holder = event.getInventory().getHolder();

            if (holder instanceof InventoryCallback && event.isShiftClick()) {
                inventoryHolder = holder;
            } else {
                return;
            }
        }

        InventoryCallback inventoryCallback = (InventoryCallback) inventoryHolder;
        inventoryCallback.onInventoryClick(event);
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder inventoryHolder = inventory.getHolder();

        if (!(inventoryHolder instanceof InventoryCallback)) {
            return;
        }

        InventoryCallback inventoryCallback = (InventoryCallback) inventoryHolder;
        inventoryCallback.onInventoryOpen(event);
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder inventoryHolder = inventory.getHolder();

        if (!(inventoryHolder instanceof InventoryCallback)) {
            return;
        }

        InventoryCallback inventoryCallback = (InventoryCallback) inventoryHolder;
        inventoryCallback.onInventoryClose(event);
    }

    @EventHandler
    private void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder inventoryHolder = inventory.getHolder();

        if (!(inventoryHolder instanceof InventoryCallback)) {
            return;
        }

        InventoryCallback inventoryCallback = (InventoryCallback) inventoryHolder;
        inventoryCallback.onInventoryDrag(event);
    }
}
