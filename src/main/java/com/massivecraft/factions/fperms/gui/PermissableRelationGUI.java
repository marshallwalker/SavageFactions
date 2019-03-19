package com.massivecraft.factions.fperms.gui;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.ServerVersion;
import com.massivecraft.factions.inventory.InventoryCallback;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.fperms.Permissable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PermissableRelationGUI extends InventoryCallback {

    private final ConfigurationSection section;
    private Inventory relationGUI;
    private FPlayer fme;
    private int guiSize;
    private HashMap<Integer, Permissable> relationSlots = new HashMap<>();


    public PermissableRelationGUI(FPlayer fme) {
        this.fme = fme;
        this.section = SavageFactionsPlugin.plugin.getConfig().getConfigurationSection("fperm-gui.relation");
    }

    public void build() {
        if (section == null) {
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Attempted to buildButton f perm GUI but config section not present.");
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Copy your config, allow the section to generate, then copy it back to your old config.");
            return;
        }

        // Build basic Inventory info
        guiSize = section.getInt("rows", 3);
        if (guiSize > 5) {
            guiSize = 5;
            SavageFactionsPlugin.plugin.log(Level.INFO, "Relation GUI size out of bounds, defaulting to 5");
        }

        guiSize *= 9;
        String guiName = ChatColor.translateAlternateColorCodes('&', section.getString("name", "FactionPermissions"));
        relationGUI = Bukkit.createInventory(this, guiSize, guiName);

        for (String key : section.getConfigurationSection("slots").getKeys(false)) {
            int slot = section.getInt("slots." + key);
            if (slot == -1) {
                continue;
            }
            if (slot + 1 > guiSize && slot > 0) {
                SavageFactionsPlugin.plugin.log(Level.WARNING, "Invalid slot of " + key.toUpperCase() + " in relation GUI skipping it");
                continue;
            }

            if (getPermissable(key) == null) {
                SavageFactionsPlugin.plugin.log(Level.WARNING, "Invalid permissable " + key.toUpperCase() + " skipping it");
                continue;
            }

            relationSlots.put(slot, getPermissable(key));
        }

        buildDummyItems();
        buildItems();
    }

    @Override
    public Inventory getInventory() {
        return relationGUI;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        event.setCancelled(true);

        if (!relationSlots.containsKey(slot)) {
            return;
        }

        PermissableActionGUI actionGUI = new PermissableActionGUI(fme, relationSlots.get(slot));
        actionGUI.build();

        fme.getPlayer().openInventory(actionGUI.getInventory());
    }

    private Permissable getPermissable(String name) {
        if (Role.fromString(name.toUpperCase()) != null) {
            return Role.fromString(name.toUpperCase());
        } else if (Relation.fromString(name.toUpperCase()) != null) {
            return Relation.fromString(name.toUpperCase());
        } else {
            return null;
        }
    }

    private void buildItems() {
        for (Map.Entry<Integer, Permissable> entry : relationSlots.entrySet()) {
            Permissable permissable = entry.getValue();

            ItemStack item = permissable.buildItem();

            if (item == null) {
                SavageFactionsPlugin.plugin.log(Level.WARNING, "Invalid material for " + permissable.toString().toUpperCase() + " skipping it");
                continue;
            }

            relationGUI.setItem(entry.getKey(), item);
        }
    }

    private void buildDummyItems() {
        if (section == null) {
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Attempted to buildButton f perm GUI but config section not present.");
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Copy your config, allow the section to generate, then copy it back to your old config.");
            return;
        }

        for (String key : section.getConfigurationSection("dummy-items").getKeys(false)) {
            int dummyId;
            try {
                dummyId = Integer.parseInt(key);
            } catch (NumberFormatException exception) {
                SavageFactionsPlugin.plugin.log(Level.WARNING, "Invalid dummy item uniqueId: " + key.toUpperCase());
                continue;
            }

            ItemStack dummyItem = buildDummyItem(dummyId);
            if (dummyItem == null) {
                continue;
            }

            ItemMeta meta = dummyItem.getItemMeta();
            if (SavageFactionsPlugin.plugin.serverVersion != ServerVersion.MC_V17) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            }

            dummyItem.setItemMeta(meta);

            List<Integer> dummySlots = section.getIntegerList("dummy-items." + key);
            for (Integer slot : dummySlots) {
                if (slot + 1 > guiSize || slot < 0) {
                    SavageFactionsPlugin.plugin.log(Level.WARNING, "Invalid slot: " + slot + " for dummy item: " + key);
                    continue;
                }
                relationGUI.setItem(slot, dummyItem);
            }
        }
    }

    private ItemStack buildDummyItem(int id) {
        final ConfigurationSection dummySection = SavageFactionsPlugin.plugin.getConfig().getConfigurationSection("fperm-gui.dummy-items." + id);

        if (dummySection == null) {
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Attempted to buildButton f perm GUI but config section not present.");
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Copy your config, allow the section to generate, then copy it back to your old config.");
            return new ItemStack(Material.AIR);
        }

        Material material = Material.matchMaterial(dummySection.getString("material", ""));
        if (material == null) {
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Invalid material for dummy item: " + id);
            return null;
        }

        ItemStack itemStack = new ItemStack(material);

        DyeColor color;
        try {
            color = DyeColor.valueOf(dummySection.getString("color", ""));
        } catch (Exception exception) {
            color = null;
        }
        if (color != null) {
            itemStack.setDurability(color.getWoolData());
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', dummySection.getString("name", " ")));

        List<String> lore = new ArrayList<>();
        for (String loreLine : dummySection.getStringList("lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
        }
        itemMeta.setLore(lore);

        if (SavageFactionsPlugin.plugin.serverVersion != ServerVersion.MC_V17) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
