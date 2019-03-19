package com.massivecraft.factions.inventory.menu;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradeConfiguration;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradeLevel;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradesConfiguration;
import com.massivecraft.factions.struct.Upgrade;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.stream.Collectors;

import static com.massivecraft.factions.struct.Upgrade.*;

public class UpgradeMenu extends Menu {
    private final UpgradesConfiguration upgradesConfiguration;

    public UpgradeMenu(SavageFactionsPlugin plugin) {
        buildInventory(this::buildInventory);

        Configuration configuration = plugin.getConfiguration();
        this.upgradesConfiguration = configuration.upgrades;

        addButton(upgradesConfiguration.crops.slot, CROP, this::buildUpgrade, this::processUpgrade);
        addButton(upgradesConfiguration.spawners.slot, SPAWNER, this::buildUpgrade, this::processUpgrade);
        addButton(upgradesConfiguration.mcmmo.slot, MCMMO, this::buildUpgrade, this::processUpgrade);
        addButton(upgradesConfiguration.slots.slot, SLOTS, this::buildUpgrade, this::processUpgrade);
    }

    private Inventory buildInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(this, 27, player.getName());

        ItemStack filler = new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"));
        ItemMeta itemMeta = filler.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "Fatal Network");
        filler.setItemMeta(itemMeta);
        filler.setDurability((short) 15);

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        return inventory;
    }

    public String replaceTokens(String text, Player player, Upgrade upgrade, int level) {
        return text
                .replace("$player", player.getName())
                .replace("$level", (level) + "")
                .replace("$upgrade", WordUtils.capitalizeFully(upgrade.name()));
    }

    private ItemStack buildUpgrade(Upgrade upgrade, MenuEvent event) {
        Player player = event.getPlayer();
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = fPlayer.getFaction();

        UpgradeConfiguration configuration = upgradesConfiguration.getUpgrade(upgrade);
        int currentLevel = faction.getUpgrade(upgrade);

        ItemStack display = configuration.display.clone();


        if (display.hasItemMeta()) {
            ItemMeta itemMeta = display.getItemMeta();

            if (itemMeta.hasDisplayName()) {
                itemMeta.setDisplayName(replaceTokens(itemMeta.getDisplayName(), player, upgrade, currentLevel));
            }

            if (itemMeta.hasLore()) {
                itemMeta.setLore(itemMeta.getLore()
                        .stream().map(line -> replaceTokens(line, player, upgrade, currentLevel))
                        .collect(Collectors.toList()));
            }

            display.setItemMeta(itemMeta);
        }

        display.setAmount(currentLevel);
        return display;
    }

    private void processUpgrade(Upgrade upgrade, MenuEvent event) {
        Player player = event.getPlayer();
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = fPlayer.getFaction();

        UpgradeConfiguration configuration = upgradesConfiguration.getUpgrade(upgrade);
        int currentLevel = faction.getUpgrade(upgrade);

        if (currentLevel == configuration.getMaxLevel()) {
            return;
        }

        UpgradeLevel upgradeLevel = configuration.getLevel(currentLevel);

        if (upgradeLevel == null) {
            return;
        }

        if (!fPlayer.canAfford(upgradeLevel.cost)) {
            return;
        }

        fPlayer.withdraw(upgradeLevel.cost);
        faction.setUpgrade(upgrade, currentLevel + 1);
    }
}
