package com.massivecraft.factions.inventory.menu;

import com.massivecraft.factions.inventory.Displayable;
import com.massivecraft.factions.inventory.InventoryCallback;
import com.massivecraft.factions.inventory.menu.component.Button;
import com.massivecraft.factions.inventory.menu.component.DataButton;
import com.massivecraft.factions.inventory.menu.component.SimpleButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class Menu extends InventoryCallback implements Displayable {
    private final Map<Integer, Button> buttons = new HashMap<>();

    private Function<Player, Inventory> inventoryBuilder;

    protected void buildInventory(Function<Player, Inventory> inventoryBuilder) {
        this.inventoryBuilder = inventoryBuilder;
    }

    protected void addButton(int slot, Button button) {
        buttons.put(slot, button);
    }

    protected void addButton(int slot, Function<MenuEvent, ItemStack> displayBuilder, Consumer<MenuEvent> action) {
        addButton(slot, new SimpleButton(displayBuilder, action));
    }

    protected <T> void addButton(int slot, T data, BiFunction<T, MenuEvent, ItemStack> displayBuilder, BiConsumer<T, MenuEvent> action) {
        addButton(slot, new DataButton<T>(data, displayBuilder, action));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        int slot = event.getSlot();

        event.setCancelled(true);

        Button button = buttons.get(slot);
        MenuEvent menuEvent = new MenuEvent(this, event);

        if (button != null) {
            button.preform(menuEvent);

            if (button.shouldUpdate(menuEvent)) {
                ItemStack newDisplay = button.getDisplay(menuEvent);
                inventory.setItem(slot, newDisplay);
            }
        }
    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void show(Player player) {
        Inventory inventory = inventoryBuilder.apply(player);

        buttons.forEach((slot, button) -> {
            MenuEvent menuEvent = new MenuEvent(this, player, slot);
            inventory.setItem(slot, button.getDisplay(menuEvent));
        });

        player.openInventory(inventory);
    }
}
