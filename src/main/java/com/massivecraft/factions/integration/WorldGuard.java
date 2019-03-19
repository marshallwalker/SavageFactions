package com.massivecraft.factions.integration;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

/*
 *  WorldGuard Region Checking
 *  Author: Spathizilla, PurePlugins
 */

public class WorldGuard {
    private static WorldGuardPlugin wg;
    private static boolean enabled;

    public static void init(Plugin plugin) {
        Plugin worldGuardPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        if (worldGuardPlugin instanceof WorldGuardPlugin) {
            SavageFactionsPlugin.plugin.log("Successfully hooked to WorldGuard.");
            wg = (WorldGuardPlugin) worldGuardPlugin;
            enabled = true;
            return;
        }

        SavageFactionsPlugin.plugin.log("Could not hook to WorldGuard. WorldGuard checks are disabled.");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean playerCanBuild(Player player, Location loc) {
        if (!enabled) {
            return false;
        }

        World world = loc.getWorld();
        Vector pt = toVector(loc);
        return wg.getRegionManager(world).getApplicableRegions(pt).size() > 0 && wg.canBuild(player, loc);
    }

    public static boolean checkForRegionsInChunk(FLocation floc) {
        Chunk chunk = floc.getWorld().getChunkAt((int) floc.getX(), (int) floc.getZ());
        return checkForRegionsInChunk(chunk);
    }

    public static boolean checkForRegionsInChunk(Location loc) {
        Chunk chunk = loc.getWorld().getChunkAt(loc);
        return checkForRegionsInChunk(chunk);
    }

    public static boolean checkForRegionsInChunk(Chunk chunk) {
        if (!enabled) {
            return false;
        }

        World world = chunk.getWorld();
        int minChunkX = chunk.getX() << 4;
        int minChunkZ = chunk.getZ() << 4;
        int maxChunkX = minChunkX + 15;
        int maxChunkZ = minChunkZ + 15;

        int worldHeight = world.getMaxHeight(); // Allow for heights other than default

        BlockVector minChunk = new BlockVector(minChunkX, 0, minChunkZ);
        BlockVector maxChunk = new BlockVector(maxChunkX, worldHeight, maxChunkZ);

        RegionManager regionManager = wg.getRegionManager(world);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("wgfactionoverlapcheck", minChunk, maxChunk);
        Map<String, ProtectedRegion> allregions = regionManager.getRegions();
        Collection<ProtectedRegion> allregionslist = new ArrayList<>(allregions.values());
        List<ProtectedRegion> overlaps;
        boolean foundregions = false;

        try {
            overlaps = region.getIntersectingRegions(allregionslist);
            foundregions = overlaps != null && !overlaps.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return foundregions;
    }
}