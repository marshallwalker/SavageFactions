package com.massivecraft.factions.listeners;

import com.massivecraft.factions.*;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradeConfiguration;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradesConfiguration;
import com.massivecraft.factions.integration.WorldGuard;
import com.massivecraft.factions.struct.*;
import com.massivecraft.factions.util.MaterialFactory;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Crops;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class FactionsBlockListener implements Listener {
    public static final Map<UUID, IWarBanner> warBanners = new HashMap<>();
    private final Map<UUID, Integer> bannerCooldowns = new HashMap<>();

    private final SavageFactionsPlugin plugin;

    private final int maxBannerAge;
    private final int bannerCooldown;

    public FactionsBlockListener(SavageFactionsPlugin plugin) {
        this.plugin = plugin;

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimer(plugin, this::updateWarBanners, 20, 20);

        this.maxBannerAge = SavageFactionsPlugin.plugin.getConfig().getInt("fbanners.Banner-Time");
        this.bannerCooldown = SavageFactionsPlugin.plugin.getConfig().getInt("fbanners.Banner-Cooldown");
    }

    private void updateWarBanners() {
        Iterator<IWarBanner> banners = warBanners.values().iterator();

        while (banners.hasNext()) {
            IWarBanner warBanner = banners.next();
            warBanner.update();

            if (warBanner.getAge() == maxBannerAge) {
                warBanner.remove();
                banners.remove();
            }
        }

        Iterator<Map.Entry<UUID, Integer>> cooldowns = bannerCooldowns.entrySet().iterator();

        while (cooldowns.hasNext()) {
            Map.Entry<UUID, Integer> next = cooldowns.next();

            next.setValue(next.getValue() - 1);

            if (next.getValue() == 0) {
                cooldowns.remove();
            }
        }
    }

    public static boolean playerCanBuildDestroyBlock(Player player, Location location, String action, boolean justCheck) {
        String name = player.getName();

        if (Conf.playersWhoBypassAllProtection.contains(name))
            return true;

        FPlayer me = FPlayers.getInstance().getById(player.getUniqueId());
        if (me.isAdminBypassing())
            return true;

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.getInstance().getFactionAt(loc);

        if (otherFaction.isWilderness()) {
            if (Conf.worldGuardBuildPriority && WorldGuard.playerCanBuild(player, location))
                return true;

            if (!Conf.wildernessDenyBuild || Conf.worldsNoWildernessProtection.contains(location.getWorld().getName()))
                return true; // This is not faction territory. Use whatever you like here.

            if (!justCheck)
                me.msg("<b>You can't " + action + " in the wilderness.");

            return false;
        } else if (otherFaction.isSafeZone()) {
            if (Conf.worldGuardBuildPriority && WorldGuard.playerCanBuild(player, location))
                return true;

            if (!Conf.safeZoneDenyBuild || Permission.MANAGE_SAFE_ZONE.has(player))
                return true;

            if (!justCheck)
                me.msg("<b>You can't " + action + " in a safe zone.");

            return false;
        } else if (otherFaction.isWarZone()) {
            if (Conf.worldGuardBuildPriority && WorldGuard.playerCanBuild(player, location))
                return true;

            if (!Conf.warZoneDenyBuild || Permission.MANAGE_WAR_ZONE.has(player))
                return true;

            if (!justCheck)
                me.msg("<b>You can't " + action + " in a war zone.");

            return false;
        }

        if (SavageFactionsPlugin.plugin.getConfig().getBoolean("hcf.raidable", false) && otherFaction.getLandRounded() > otherFaction.getPowerRounded())
            return true;

        Faction myFaction = me.getFaction();
        Relation rel = myFaction.getRelationTo(otherFaction);
        boolean online = otherFaction.hasPlayersOnline();
        boolean pain = !justCheck && rel.confPainBuild(online);
        boolean deny = rel.confDenyBuild(online);

        Access access = otherFaction.getAccess(me, PermissableAction.fromString(action));
        if (access == Access.ALLOW && ((rel == Relation.ALLY) || (rel == Relation.ENEMY) || (rel == Relation.NEUTRAL) || (rel == Relation.TRUCE)))
            deny = false;

        // hurt the player for building/destroying in other territory?
        if (pain) {
            player.damage(Conf.actionDeniedPainAmount);

            if (!deny) {
                me.msg("<b>It is painful to try to " + action + " in the territory of " + otherFaction.getTag(myFaction));
            }
        }


        // cancel building/destroying in other territory?
        if (deny) {
            if (!justCheck) {
                me.msg("<b>You can't " + action + " in the territory of " + otherFaction.getTag(myFaction));
            }

            return false;
        }

        // Also cancel and/or cause pain if player doesn't have ownership rights for this claim
        if (Conf.ownedAreasEnabled && (Conf.ownedAreaDenyBuild || Conf.ownedAreaPainBuild) && !otherFaction.playerHasOwnershipRights(me, loc)) {
            if (!pain && Conf.ownedAreaPainBuild && !justCheck) {
                player.damage(Conf.actionDeniedPainAmount);
                if (!Conf.ownedAreaDenyBuild) {
                    me.msg("<b>It is painful to try to " + action + " in this territory, it is owned by: " + otherFaction.getOwnerListString(loc));
                }
            }
            if (Conf.ownedAreaDenyBuild) {
                if (!justCheck) {
                    me.msg("<b>You can't " + action + " in this territory, it is owned by: " + otherFaction.getOwnerListString(loc));
                    return false;
                }
            }
        }

        // Check the permission just after making sure the land isn't owned by someone else to avoid bypass.

        if (access != Access.ALLOW && me.getRole() != Role.LEADER) {
            // TODO: Update this once new access values are added other than just allow / deny.
            if (access == Access.DENY) {
                if (!justCheck)
                    me.msg(TL.GENERIC_NOPERMISSION, action);
                return false;
            } else if (myFaction.getOwnerListString(loc) != null && !myFaction.getOwnerListString(loc).isEmpty() && !myFaction.getOwnerListString(loc).contains(player.getName())) {
                if (!justCheck)
                    me.msg("<b>You can't " + action + " in this territory, it is owned by: " + myFaction.getOwnerListString(loc));
                return false;
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.canBuild()) {
            return;
        }

        // special case for flint&steel, which should only be prevented by DenyUsage list
        if (event.getBlockPlaced().getType() == Material.FIRE) {
            return;
        }

        if (!playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getLocation(), "buildButton", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!Conf.handleExploitLiquidFlow) {
            return;
        }
        if (event.getBlock().isLiquid()) {
            if (event.getToBlock().isEmpty()) {
                Faction from = Board.getInstance().getFactionAt(new FLocation(event.getBlock()));
                Faction to = Board.getInstance().getFactionAt(new FLocation(event.getToBlock()));
                if (from == to) {
                    // not concerned with inter-faction events
                    return;
                }
                // from faction != to faction
                if (to.isNormal()) {
                    if (from.isNormal() && from.getRelationTo(to).isAlly()) {
                        return;
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.getInstaBreak() && !playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getLocation(), "destroy", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!Conf.pistonProtectionThroughDenyBuild) {
            return;
        }

        Faction pistonFaction = Board.getInstance().getFactionAt(new FLocation(event.getBlock()));

        // target end-of-the-line empty (air) block which is being pushed into, including if piston itself would extend into air
        Block targetBlock = event.getBlock().getRelative(event.getDirection(), event.getLength() + 1);

        // if potentially pushing into air/water/lava in another territory, we need to check it out
        if ((targetBlock.isEmpty() || targetBlock.isLiquid()) && !canPistonMoveBlock(pistonFaction, targetBlock.getLocation())) {
            event.setCancelled(true);
        }

        /*
         * note that I originally was testing the territory of each affected block, but since I found that pistons can only push
         * up to 12 blocks and the width of any territory is 16 blocks, it should be safe (and much more lightweight) to test
         * only the final target block as done above
         */
    }

    @EventHandler
    public void onVaultPlace(BlockPlaceEvent e) {
        if (e.getItemInHand().getType() != Material.CHEST) {
            return;
        }

        ItemStack vault = SavageFactionsPlugin.plugin.createItem(Material.CHEST, 1, (short) 0, SavageFactionsPlugin.plugin.color(SavageFactionsPlugin.plugin.getConfig().getString("fvault.Item.Name")), SavageFactionsPlugin.plugin.colorList(SavageFactionsPlugin.plugin.getConfig().getStringList("fvault.Item.Lore")));
        if (e.getItemInHand().isSimilar(vault)) {
            FPlayer fme = FPlayers.getInstance().getByPlayer(e.getPlayer());
            if (fme.getFaction().getVault() != null) {
                fme.msg(TL.COMMAND_GETVAULT_ALREADYSET);
                e.setCancelled(true);
                return;
            }
            FLocation flocation = new FLocation(e.getBlockPlaced().getLocation());
            if (Board.getInstance().getFactionAt(flocation) != fme.getFaction()) {
                fme.msg(TL.COMMAND_GETVAULT_INVALIDLOCATION);
                e.setCancelled(true);
                return;
            }
            Block start = e.getBlockPlaced();
            int radius = 1;
            for (double x = start.getLocation().getX() - radius; x <= start.getLocation().getX() + radius; x++) {
                for (double y = start.getLocation().getY() - radius; y <= start.getLocation().getY() + radius; y++) {
                    for (double z = start.getLocation().getZ() - radius; z <= start.getLocation().getZ() + radius; z++) {
                        Location blockLoc = new Location(e.getPlayer().getWorld(), x, y, z);
                        if (blockLoc.getX() == start.getLocation().getX() && blockLoc.getY() == start.getLocation().getY() && blockLoc.getZ() == start.getLocation().getZ()) {
                            continue;
                        }

                        Material blockMaterial = blockLoc.getBlock().getType();

                        if (blockMaterial == Material.CHEST || (SavageFactionsPlugin.plugin.getConfig().getBoolean("fvault.No-Hoppers-near-vault") && blockMaterial == Material.HOPPER)) {
                            e.setCancelled(true);
                            fme.msg(TL.COMMAND_GETVAULT_CHESTNEAR);
                            return;
                        }
                    }
                }
            }

            fme.msg(TL.COMMAND_GETVAULT_SUCCESS);
            fme.getFaction().setVault(e.getBlockPlaced().getLocation());
        }
    }

    @EventHandler
    public void onHopperPlace(BlockPlaceEvent e) {

        if (e.getItemInHand().getType() != Material.HOPPER && !SavageFactionsPlugin.plugin.getConfig().getBoolean("fvault.No-Hoppers-near-vault")) {
            return;
        }

        Faction factionAt = Board.getInstance().getFactionAt(new FLocation(e.getBlockPlaced().getLocation()));

        if (factionAt.isWilderness() || factionAt.getVault() == null) {
            return;
        }


        FPlayer fme = FPlayers.getInstance().getByPlayer(e.getPlayer());

        Block start = e.getBlockPlaced();
        int radius = 1;
        for (double x = start.getLocation().getX() - radius; x <= start.getLocation().getX() + radius; x++) {
            for (double y = start.getLocation().getY() - radius; y <= start.getLocation().getY() + radius; y++) {
                for (double z = start.getLocation().getZ() - radius; z <= start.getLocation().getZ() + radius; z++) {
                    Location blockLoc = new Location(e.getPlayer().getWorld(), x, y, z);
                    if (blockLoc.getX() == start.getLocation().getX() && blockLoc.getY() == start.getLocation().getY() && blockLoc.getZ() == start.getLocation().getZ()) {
                        continue;
                    }

                    if (blockLoc.getBlock().getType() == Material.CHEST) {
                        if (factionAt.getVault().equals(blockLoc)) {
                            e.setCancelled(true);
                            fme.msg(TL.COMMAND_VAULT_NO_HOPPER);
                            return;
                        }
                    }
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        // if not a sticky piston, retraction should be fine
        if (!event.isSticky() || !Conf.pistonProtectionThroughDenyBuild) {
            return;
        }

        Location targetLoc = event.getRetractLocation();
        Faction otherFaction = Board.getInstance().getFactionAt(new FLocation(targetLoc));

        // Check if the piston is moving in a faction's territory. This disables pistons entirely in faction territory.
        if (otherFaction.isNormal() && SavageFactionsPlugin.plugin.getConfig().getBoolean("disable-pistons-in-territory", false)) {
            event.setCancelled(true);
            return;
        }

        // if potentially retracted block is just air/water/lava, no worries
        if (targetLoc.getBlock().isEmpty() || targetLoc.getBlock().isLiquid()) {
            return;
        }

        Faction pistonFaction = Board.getInstance().getFactionAt(new FLocation(event.getBlock()));

        if (!canPistonMoveBlock(pistonFaction, targetLoc)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBannerPlace(BlockPlaceEvent event) {
        if (SavageFactionsPlugin.plugin.serverVersion == ServerVersion.MC_V17 || event.getItemInHand().getType() != SavageFactionsPlugin.plugin.BANNER) {
            return;
        }

        ItemStack itemInHand = event.getItemInHand();
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(event.getPlayer());
        ItemStack factionBanner = fPlayer.getFaction().getBanner();

        if (factionBanner == null) {
            factionBanner = new ItemStack(SavageFactionsPlugin.plugin.BANNER);
        }

        ItemMeta bannerItemMeta = factionBanner.getItemMeta();
        bannerItemMeta.setDisplayName(SavageFactionsPlugin.plugin.color(SavageFactionsPlugin.plugin.getConfig().getString("fbanners.Item.Name")));
        bannerItemMeta.setLore(SavageFactionsPlugin.plugin.colorList(SavageFactionsPlugin.plugin.getConfig().getStringList("fbanners.Item.Lore")));
        factionBanner.setItemMeta(bannerItemMeta);

        if (!factionBanner.isSimilar(itemInHand)) {
            return;
        }

        if (fPlayer.getFaction().isWilderness()) {
            fPlayer.msg(TL.WARBANNER_NOFACTION);
            event.setCancelled(true);
            return;
        }

        Location location = event.getBlockPlaced().getLocation();
        FLocation fLocation = new FLocation(location);
        Faction factionAt = Board.getInstance().getFactionAt(fLocation);

        boolean canPlace = (factionAt.isWarZone() && SavageFactionsPlugin.plugin.getConfig().getBoolean("fbanners.Placeable.Warzone") ||
                (fPlayer.getFaction().getRelationTo(factionAt) == Relation.ENEMY) && SavageFactionsPlugin.plugin.getConfig().getBoolean("fbanners.Placeable.Enemy"));

        if (!canPlace) {
            fPlayer.msg(TL.WARBANNER_INVALIDLOC);
            event.setCancelled(true);
            return;
        }

        if (bannerCooldowns.containsKey(fPlayer.getFactionId())) {
            fPlayer.msg(TL.WARBANNER_COOLDOWN);
            event.setCancelled(true);
            return;
        }

        String title = SavageFactionsPlugin.plugin.color(fPlayer.getTag() + " Placed A WarBanner!");
        String subtitle = SavageFactionsPlugin.plugin.color("&7use &c/f tpbanner&7 to tp to the banner!");
        int fadeIn = SavageFactionsPlugin.plugin.getConfig().getInt("Title.Options.FadeInTime");
        int showTime = SavageFactionsPlugin.plugin.getConfig().getInt("Title.Options.ShowTime");
        int fadeOut = SavageFactionsPlugin.plugin.getConfig().getInt("Title.Options.FadeOutTime");

        for (FPlayer fplayer : fPlayer.getFaction().getFPlayers()) {
            if (SavageFactionsPlugin.plugin.serverVersion == ServerVersion.MC_V18) {
                fplayer.getPlayer().sendTitle(title, subtitle);
            } else {
                fplayer.getPlayer().sendTitle(title, subtitle, fadeIn, showTime, fadeOut);
            }
        }

        IWarBanner newWarBanner = new WarBanner(fPlayer.getFaction(), location);
        newWarBanner.spawn();

        warBanners.put(fPlayer.getFactionId(), newWarBanner);
        bannerCooldowns.put(fPlayer.getFactionId(), bannerCooldown);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFrostWalker(EntityBlockFormEvent event) {
        if (event.getEntity() == null || event.getEntity().getType() != EntityType.PLAYER || event.getBlock() == null) {
            return;
        }

        Player player = (Player) event.getEntity();
        Location location = event.getBlock().getLocation();

        // only notify every 10 seconds
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        boolean justCheck = fPlayer.getLastFrostwalkerMessage() + 10000 > System.currentTimeMillis();

        if (!justCheck) {
            fPlayer.setLastFrostwalkerMessage();
        }

        // Check if they have buildButton permissions here. If not, block this from happening.
        if (!playerCanBuildDestroyBlock(player, location, "frostwalk", justCheck)) {
            event.setCancelled(true);
        }
    }

    private boolean canPistonMoveBlock(Faction pistonFaction, Location target) {
        Faction otherFaction = Board.getInstance().getFactionAt(new FLocation(target));

        if (pistonFaction == otherFaction) {
            return true;
        }

        if (otherFaction.isWilderness()) {
            return !Conf.wildernessDenyBuild || Conf.worldsNoWildernessProtection.contains(target.getWorld().getName());

        } else if (otherFaction.isSafeZone()) {
            return !Conf.safeZoneDenyBuild;

        } else if (otherFaction.isWarZone()) {
            return !Conf.warZoneDenyBuild;
        }

        Relation rel = pistonFaction.getRelationTo(otherFaction);
        return !rel.confDenyBuild(otherFaction.hasPlayersOnline());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!playerCanBuildDestroyBlock(player, block.getLocation(), "destroy", false)) {
            event.setCancelled(true);
            return;
        }

        FPlayer fme = FPlayers.getInstance().getByPlayer(player);

        if (!fme.hasFaction() || block.getType() == SavageFactionsPlugin.plugin.MOB_SPANWER) {
            return;
        }

        if (!fme.isAdminBypassing()) {
            Access access = fme.getFaction().getAccess(fme, PermissableAction.SPAWNER);

            if (access != Access.ALLOW && fme.getRole() != Role.LEADER) {
                fme.msg(TL.GENERIC_FPERM_NOPERMISSION, "mine spawners");
            }
        }
    }

    @EventHandler
    public void onFarmLandDamage(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;

        if (playerCanBuildDestroyBlock(player, event.getBlock().getLocation(), PermissableAction.DESTROY.name(), true)) {
            return;
        }

        FPlayer fPlayer = FPlayers.getInstance().getById(player.getUniqueId());
        Faction otherFaction = Board.getInstance().getFactionAt(new FLocation(event.getBlock().getLocation()));
        Faction myFaction = fPlayer.getFaction();

        fPlayer.msg("<b>You can't jump on farmland in the territory of " + otherFaction.getTag(myFaction));
        event.setCancelled(true);
    }

    ////////////////////////////////////////////////////
    //  FACTION CROP UPGRADES
    ////////////////////////////////////////////////////
    @EventHandler
    private void onBlockGrow(BlockGrowEvent event) {
        Configuration configuration = plugin.getConfiguration();
        UpgradesConfiguration upgrades = configuration.upgrades;
        UpgradeConfiguration crops = upgrades.crops;

        if(!upgrades.enabled || !crops.enabled) {
            return;
        }

        Block block = event.getBlock();
        FLocation fLocation = new FLocation(block.getLocation());
        Faction factionAt = Board.getInstance().getFactionAt(fLocation);

        if (factionAt.isWilderness()) {
            return;
        }

        int level = factionAt.getUpgrade(Upgrade.CROP);

        if (level <= 0) {
            return;
        }

        double boost = crops.levels.get(level).boost;
        boolean ripen = ThreadLocalRandom.current().nextDouble() < boost;

        if(!ripen) {
            return;
        }

        if (block.getType() != MaterialFactory.LEGACY_CROPS) {
            return;
        }

        event.setCancelled(true);

        BlockState blockState = block.getState();
        Crops blockData = (Crops) blockState.getBlockData();
        blockData.setState(CropState.RIPE);
        blockState.setData(blockData);
        blockState.update();

        Block below = block.getRelative(BlockFace.DOWN);

        Material SUGAR_CANE_BLOCK = SavageFactionsPlugin.plugin.SUGAR_CANE_BLOCK;

        if ((below.getType() == SUGAR_CANE_BLOCK || below.getType() == Material.CACTUS) && below.getType() == block.getType()) {
            Block above = block.getRelative(BlockFace.UP);

            if (above.getType() == Material.AIR) {
                above.setType(block.getType());
            }
        }
    }
}
