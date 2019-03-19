package com.massivecraft.factions.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public interface InventoryEventConsumer {

    void onInventoryClick(InventoryClickEvent event);

    void onInventoryOpen(InventoryOpenEvent event);

    void onInventoryClose(InventoryCloseEvent event);

    void onInventoryDrag(InventoryDragEvent event);
}
