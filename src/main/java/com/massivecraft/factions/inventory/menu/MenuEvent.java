package com.massivecraft.factions.inventory.menu;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

@Getter
public class MenuEvent {
    private final Menu menu;
    private final Player player;
    private final int slot;
    private final ClickType clickType;

    public MenuEvent(Menu menu, InventoryClickEvent event) {
        this.menu = menu;
        this.player = (Player) event.getWhoClicked();
        this.slot = event.getSlot();
        this.clickType = event.getClick();
    }

    public MenuEvent(Menu menu, Player player, int slot) {
        this.menu = menu;
        this.player = player;
        this.slot = slot;
        this.clickType = ClickType.UNKNOWN;
    }
}
