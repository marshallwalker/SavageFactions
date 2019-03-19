package com.massivecraft.factions.fperms.gui;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.ServerVersion;
import com.massivecraft.factions.inventory.InventoryCallback;
import com.massivecraft.factions.struct.Access;
import com.massivecraft.factions.fperms.Permissable;
import com.massivecraft.factions.struct.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;

public class PermissableActionGUI extends InventoryCallback {

    private final ConfigurationSection section;
    private Inventory actionGUI;
    private FPlayer fme;
    private int guiSize;
    private Permissable permissable;
    private HashMap<Integer, PermissableAction> actionSlots = new HashMap<>();
    private HashMap<Integer, SpecialItem> specialSlots = new HashMap<>();
    private ArrayList<Integer> usedDummySlots = new ArrayList<>();

    public PermissableActionGUI(FPlayer fme, Permissable permissable) {
        this.fme = fme;
        this.permissable = permissable;
        this.section = SavageFactionsPlugin.plugin.getConfig().getConfigurationSection("fperm-gui.action");
    }

    public void build() {
        if (section == null) {
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Attempted to buildButton f perm GUI but config section not present.");
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Copy your config, allow the section to generate, then copy it back to your old config.");
            return;
        }

        guiSize = section.getInt("rows", 3);
        if (guiSize > 6) {
            guiSize = 6;
            SavageFactionsPlugin.plugin.log(Level.INFO, "Action GUI size out of bounds, defaulting to 6");
        }

        guiSize *= 9;
        String guiName = ChatColor.translateAlternateColorCodes('&', section.getString("name", "FactionPerms"));
        actionGUI = Bukkit.createInventory(this, guiSize, guiName);
        boolean disabled = false;
        for (String key : section.getConfigurationSection("slots").getKeys(false)) {
            int slot = section.getInt("slots." + key);
            if (slot == -1) {
                disabled = true;
                continue;
            }
            if (slot + 1 > guiSize || slot < 0) {
                SavageFactionsPlugin.plugin.log(Level.WARNING, "Invalid slot for: " + key.toUpperCase());
                continue;
            }

            if (SpecialItem.isSpecial(key)) {
                specialSlots.put(slot, SpecialItem.fromString(key));
                continue;
            }

            PermissableAction permissableAction = PermissableAction.fromString(key.toUpperCase().replace('-', '_'));
            if (permissableAction == null) {
                SavageFactionsPlugin.plugin.log(Level.WARNING, "Invalid permissable action: " + key.toUpperCase());
                continue;
            }

            actionSlots.put(section.getInt("slots." + key), permissableAction);
        }

        buildDummyItems();

        if (actionSlots.values().toArray().length != PermissableAction.values().length) {
            // Missing actions add them forcefully to the GUI and log error
            Set<PermissableAction> missingActions = new HashSet<>(Arrays.asList(PermissableAction.values()));
            missingActions.removeAll(actionSlots.values());

            for (PermissableAction action : missingActions) {
                if (disabled) {
                    break;
                }
                if (!usedDummySlots.isEmpty()) {
                    int slot = usedDummySlots.get(0);
                    actionSlots.put(slot, action);
                } else {
                    int slot = actionGUI.firstEmpty();
                    if (slot != -1) {
                        actionSlots.put(slot, action);
                    }
                }
                SavageFactionsPlugin.plugin.log(Level.WARNING, "Missing action: " + action.name());
            }

        }

        buildSpecialItems();
        buildItems();
    }

    @Override
    public Inventory getInventory() {
        return actionGUI;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        ClickType click = event.getClick();

        event.setCancelled(true);

        if (specialSlots.containsKey(slot)) {
            if (specialSlots.get(slot) == SpecialItem.BACK) {
                PermissableRelationGUI relationGUI = new PermissableRelationGUI(fme);
                relationGUI.build();

                fme.getPlayer().openInventory(relationGUI.getInventory());
            }
            return;
        }
        if (!actionSlots.containsKey(slot)) {
            return;
        }

        PermissableAction action = actionSlots.get(slot);
        Access access;
        if (click == ClickType.LEFT) {
            access = Access.ALLOW;
            fme.getFaction().setPermission(permissable, action, access);
        } else if (click == ClickType.RIGHT) {
            access = Access.DENY;
            fme.getFaction().setPermission(permissable, action, access);
        } else if (click == ClickType.MIDDLE) {
            access = Access.UNDEFINED;
            fme.getFaction().setPermission(permissable, action, access);
        } else {
            return;
        }

        actionGUI.setItem(slot, action.buildItem(fme, permissable));
        fme.msg(TL.COMMAND_PERM_SET, action.name(), access.name(), permissable.name());
        SavageFactionsPlugin.plugin.log(String.format(TL.COMMAND_PERM_SET.toString(), action.name(), access.name(), permissable.name()) + " for faction " + fme.getTag());
    }

    private void buildItems() {
        for (Map.Entry<Integer, PermissableAction> entry : actionSlots.entrySet()) {
            PermissableAction permissableAction = entry.getValue();

            ItemStack item = permissableAction.buildItem(fme, permissable);

            if (item == null) {
                SavageFactionsPlugin.plugin.log(Level.WARNING, "Invalid item for: " + permissableAction.toString().toUpperCase());
                continue;
            }

            actionGUI.setItem(entry.getKey(), item);
        }
    }

    private void buildSpecialItems() {
        for (Map.Entry<Integer, SpecialItem> entry : specialSlots.entrySet()) {
            actionGUI.setItem(entry.getKey(), getSpecialItem(entry.getValue()));
        }
    }

    private ItemStack getSpecialItem(SpecialItem specialItem) {
        if (section == null) {
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Attempted to buildButton f perm GUI but config section not present.");
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Copy your config, allow the section to generate, then copy it back to your old config.");
            return new ItemStack(Material.AIR);
        }

        switch (specialItem) {
            case RELATION:
                return permissable.buildItem();
            case BACK:
                ConfigurationSection backButtonConfig = SavageFactionsPlugin.plugin.getConfig().getConfigurationSection("fperm-gui.back-item");

                ItemStack backButton = new ItemStack(Material.matchMaterial(backButtonConfig.getString("material")));
                ItemMeta backButtonMeta = backButton.getItemMeta();

                backButtonMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', backButtonConfig.getString("name")));
                List<String> lore = new ArrayList<>();
                for (String loreLine : backButtonConfig.getStringList("lore")) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
                }

                backButtonMeta.setLore(lore);
                if (SavageFactionsPlugin.plugin.serverVersion != ServerVersion.MC_V17) {
                    backButtonMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
                }

                backButton.setItemMeta(backButtonMeta);

                return backButton;
            default:
                return null;
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
                usedDummySlots.add(slot);
                actionGUI.setItem(slot, dummyItem);
            }
        }
    }

    private ItemStack buildDummyItem(int id) {
        final ConfigurationSection dummySection = SavageFactionsPlugin.plugin.getConfig().getConfigurationSection("fperm-gui.dummy-items." + id);

        if (dummySection == null) {
            SavageFactionsPlugin.plugin.log(Level.WARNING, "Attempted to buildButton dummy items for F PERM GUI but config section not present.");
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

    public enum SpecialItem {
        BACK,
        RELATION;

        static boolean isSpecial(String string) {
            return fromString(string) != null;
        }

        static SpecialItem fromString(String string) {
            for (SpecialItem specialItem : SpecialItem.values()) {
                if (string.equalsIgnoreCase(specialItem.name())) {
                    return specialItem;
                }
            }
            return null;
        }
    }

}
