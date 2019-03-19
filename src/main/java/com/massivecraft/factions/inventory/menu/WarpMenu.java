package com.massivecraft.factions.inventory.menu;

import com.massivecraft.factions.*;
import com.massivecraft.factions.configuration.implementation.warp.WarpConfiguration;
import com.massivecraft.factions.configuration.implementation.warp.WarpCostConfiguration;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.util.WarmUpUtil;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WarpMenu extends Menu {
    private final WarpConfiguration configuration;

    public WarpMenu(SavageFactionsPlugin plugin) {
        this.configuration = plugin.getConfiguration().warps;

        buildInventory(this::buildInventory);
    }

    private Inventory buildInventory(Player player) {
        String title = configuration.menu.title;
        int size = configuration.menu.size;

        Inventory inventory = Bukkit.createInventory(this, size, title);

        ItemStack filler = new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"));
        ItemMeta itemMeta = filler.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET + "Fatal Network");
        filler.setItemMeta(itemMeta);
        filler.setDurability((short) 15);

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = fPlayer.getFaction();
        Collection<FWarp> warps = faction.getWarps().values();

        int slot = 10;

        for (FWarp warp : warps) {
            addButton(slot, warp, this::buildWarpDisplay, this::preformWarp);
            slot++;
        }

        return inventory;
    }

    private ItemStack buildWarpDisplay(FWarp fWarp, MenuEvent event) {
        LazyLocation location = fWarp.getLocation();

        ItemStack itemStack = new ItemStack(Material.ENDER_PEARL);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + fWarp.getName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Has Password: " + ChatColor.RED + fWarp.hasPassword());

        if (!fWarp.hasPassword()) {
            lore.add("");

            int x = (int) Math.floor(location.getX());
            int y = (int) Math.floor(location.getY());
            int z = (int) Math.floor(location.getZ());

            lore.add(ChatColor.GRAY + "World: " + location.getWorldName());
            lore.add(ChatColor.GRAY + "X: " + x + ", Y: " + y + ", Z: " + z);
        }

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void cancelledPasswordAttempt(FPlayer fPlayer) {
        fPlayer.msg(TL.COMMAND_FWARP_PASSWORD_TIMEOUT);
    }

    private void processPasswordAttempt(FPlayer fPlayer, FWarp warp, String password) {
        if (!warp.isPassword(password)) {
            fPlayer.msg(TL.COMMAND_FWARP_INVALID_PASSWORD);
        } else {

            if(!attemptTransaction(fPlayer)) {
                return;
            }

            WarmUpUtil.process(fPlayer, WarmUpUtil.Warmup.WARP, TL.WARMUPS_NOTIFY_TELEPORT, warp.getName(), () -> {
                fPlayer.msg(TL.COMMAND_FWARP_WARPED, warp.getName());
                fPlayer.getPlayer().teleport(warp.getLocation().getLocation());
            }, configuration.warmup);
        }

        fPlayer.setChatCallback(null);
    }

    private void preformWarp(FWarp warp, MenuEvent event) {
        Player player = event.getPlayer();
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        player.closeInventory();

        if (!warp.hasPassword()) {
            processPasswordAttempt(fPlayer, warp, "");
            return;
        }

        fPlayer.msg(TL.COMMAND_FWARP_PASSWORD_REQUIRED);
        fPlayer.setChatCallback(result ->
                processPasswordAttempt(fPlayer, warp, result), () ->
                cancelledPasswordAttempt(fPlayer), 8, TimeUnit.SECONDS);
    }

    private boolean attemptTransaction(FPlayer fplayer) {
        WarpCostConfiguration warpCosts = configuration.cost;

        if (!warpCosts.enabled || fplayer.isAdminBypassing()) {
            return true;
        }

        return Econ.attemptPayment(fplayer, warpCosts.use, TL.COMMAND_FWARP_TOWARP, TL.COMMAND_FWARP_FORWARPING);
    }
}
