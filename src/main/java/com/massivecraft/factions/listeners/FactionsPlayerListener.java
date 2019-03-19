package com.massivecraft.factions.listeners;

import com.earth2me.essentials.User;
import com.massivecraft.factions.*;
import com.massivecraft.factions.cmd.CmdFly;
import com.massivecraft.factions.cmd.CmdSeeChunk;
import com.massivecraft.factions.event.FPlayerEnteredFactionEvent;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.scoreboards.FScoreboard;
import com.massivecraft.factions.scoreboards.FTeamWrapper;
import com.massivecraft.factions.scoreboards.sidebar.FDefaultSidebar;
import com.massivecraft.factions.struct.*;
import com.massivecraft.factions.util.MultiversionMaterials;
import com.massivecraft.factions.util.VisualizeUtil;
import com.massivecraft.factions.zcore.persist.MemoryFPlayer;
import com.massivecraft.factions.zcore.util.TL;
import com.massivecraft.factions.zcore.util.TagUtil;
import com.massivecraft.factions.zcore.util.TextUtil;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.NumberConversions;

import java.util.*;
import java.util.logging.Level;

public class FactionsPlayerListener implements Listener {
    HashMap<Player, Boolean> fallMap = new HashMap<>();

    // Holds the next time a player can have a map shown.
    private HashMap<UUID, Long> showTimes = new HashMap<>();
    // for handling people who repeatedly spam attempts to open a door (or similar) in another faction's territory
    private Map<String, InteractAttemptSpam> interactSpammers = new HashMap<>();

    public FactionsPlayerListener() {
        for (Player player : SavageFactionsPlugin.plugin.getServer().getOnlinePlayers()) {
            initPlayer(player);
        }
    }

    public static Boolean isSystemFaction(Faction faction) {
        return faction.isSafeZone() || faction.isWarZone() || faction.isWilderness();
    }

    public static boolean playerCanUseItemHere(Player player, Location location, Material material, boolean justCheck) {
        String name = player.getName();
        if (Conf.playersWhoBypassAllProtection.contains(name)) {
            return true;
        }


        FPlayer me = FPlayers.getInstance().getByPlayer(player);
        if (me.isAdminBypassing()) {
            return true;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.getInstance().getFactionAt(loc);
        Faction myFaction = me.getFaction();
        Relation rel = myFaction.getRelationTo(otherFaction);

        // Also cancel if player doesn't have ownership rights for this claim
        if (Conf.ownedAreasEnabled && myFaction == otherFaction && !myFaction.playerHasOwnershipRights(me, loc)) {
            if (!justCheck) {
                me.msg("<b>You can't use that in this territory, it is owned by: " + otherFaction.getOwnerListString(loc));
            }
            return false;
        }

        if (SavageFactionsPlugin.plugin.getConfig().getBoolean("hcf.raidable", false) && otherFaction.getLandRounded() > otherFaction.getPowerRounded()) {
            return true;
        }

        if (otherFaction.hasPlayersOnline()) {
            if (!Conf.territoryDenyUseageMaterials.contains(material)) {
                return true; // Item isn't one we're preventing for online factions.
            }
        } else {
            if (!Conf.territoryDenyUseageMaterialsWhenOffline.contains(material)) {
                return true; // Item isn't one we're preventing for offline factions.
            }
        }

        if (otherFaction.isWilderness()) {
            if (!Conf.wildernessDenyUseage || Conf.worldsNoWildernessProtection.contains(location.getWorld().getName())) {
                return true; // This is not faction territory. Use whatever you like here.
            }

            if (!justCheck) {
                me.msg(TL.PLAYER_USE_WILDERNESS, TextUtil.getMaterialName(material));
            }

            return false;
        } else if (otherFaction.isSafeZone()) {
            if (!Conf.safeZoneDenyUseage || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return true;
            }

            if (!justCheck) {
                me.msg(TL.PLAYER_USE_SAFEZONE, TextUtil.getMaterialName(material));
            }

            return false;
        } else if (otherFaction.isWarZone()) {
            if (!Conf.warZoneDenyUseage || Permission.MANAGE_WAR_ZONE.has(player)) {
                return true;
            }

            if (!justCheck) {
                me.msg(TL.PLAYER_USE_WARZONE, TextUtil.getMaterialName(material));
            }

            return false;
        }

        // Cancel if we are not in our own territory
        if (rel.confDenyUseage()) {
            if (!justCheck) {
                me.msg(TL.PLAYER_USE_TERRITORY, TextUtil.getMaterialName(material), otherFaction.getTag(myFaction));
            }

            return false;
        }

        Access access = otherFaction.getAccess(me, PermissableAction.ITEM);
        if (access != null && access != Access.UNDEFINED) {
            // TODO: Update this once new access values are added other than just allow / deny.
            if ((myFaction.getOwnerListString(loc) != null && !myFaction.getOwnerListString(loc).isEmpty() && myFaction.getOwnerListString(loc).contains(player.getName()))) {
                return true;
            } else if (myFaction.getOwnerListString(loc) != null && !myFaction.getOwnerListString(loc).isEmpty() && !myFaction.getOwnerListString(loc).contains(player.getName())) {
                me.msg("<b>You can't use items in this territory, it is owned by: " + myFaction.getOwnerListString(loc));
                return false;
            } else if (access == Access.DENY) {
                me.msg(TL.GENERIC_NOPERMISSION, PermissableAction.ITEM);
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    public static boolean canPlayerUseBlock(Player player, Block block, boolean justCheck) {
        if (Conf.playersWhoBypassAllProtection.contains(player.getName()))
            return true;

        FPlayer me = FPlayers.getInstance().getByPlayer(player);
        if (me.isAdminBypassing())
            return true;

        Material material = block.getType();
        // Dupe fix.
        FLocation loc = new FLocation(block);
        Faction otherFaction = Board.getInstance().getFactionAt(loc);
        Faction myFaction = me.getFaction();
        Relation rel = myFaction.getRelationTo(otherFaction);

        // no door/chest/whatever protection in wilderness, war zones, or safe zones
        if (!otherFaction.isNormal())
            return true;

        if (SavageFactionsPlugin.plugin.getConfig().getBoolean("hcf.raidable", false) && otherFaction.getLandRounded() > otherFaction.getPowerRounded())
            return true;

        if (!rel.isMember() || !otherFaction.playerHasOwnershipRights(me, loc) && player.getItemInHand().getType() != null) {

            if (player.getItemInHand().getType().toString().toUpperCase().contains("DOOR"))
                return false;
        }

        PermissableAction action = null;

        if (SavageFactionsPlugin.plugin.serverVersion == ServerVersion.MC_V1_13) {
            switch (block.getType()) {
                case LEVER:
                    action = PermissableAction.LEVER;
                    break;

                case ACACIA_BUTTON:
                case BIRCH_BUTTON:
                case DARK_OAK_BUTTON:
                case JUNGLE_BUTTON:
                case OAK_BUTTON:
                case SPRUCE_BUTTON:
                case STONE_BUTTON:
                    action = PermissableAction.BUTTON;
                    break;

                case ACACIA_DOOR:
                case BIRCH_DOOR:
                case IRON_DOOR:
                case JUNGLE_DOOR:
                case OAK_DOOR:
                case SPRUCE_DOOR:
                case DARK_OAK_DOOR:

                case ACACIA_TRAPDOOR:
                case BIRCH_TRAPDOOR:
                case DARK_OAK_TRAPDOOR:
                case IRON_TRAPDOOR:
                case JUNGLE_TRAPDOOR:
                case OAK_TRAPDOOR:
                case SPRUCE_TRAPDOOR:

                case ACACIA_FENCE_GATE:
                case BIRCH_FENCE_GATE:
                case DARK_OAK_FENCE_GATE:
                case JUNGLE_FENCE_GATE:
                case OAK_FENCE_GATE:
                case SPRUCE_FENCE_GATE:
                    action = PermissableAction.DOOR;
                    break;

                case CHEST:
                case TRAPPED_CHEST:
                case CHEST_MINECART:

                case SHULKER_BOX:
                case BLACK_SHULKER_BOX:
                case BLUE_SHULKER_BOX:
                case BROWN_SHULKER_BOX:
                case CYAN_SHULKER_BOX:
                case GRAY_SHULKER_BOX:
                case GREEN_SHULKER_BOX:
                case LIGHT_BLUE_SHULKER_BOX:
                case LIGHT_GRAY_SHULKER_BOX:
                case LIME_SHULKER_BOX:
                case MAGENTA_SHULKER_BOX:
                case ORANGE_SHULKER_BOX:
                case PINK_SHULKER_BOX:
                case PURPLE_SHULKER_BOX:
                case RED_SHULKER_BOX:
                case WHITE_SHULKER_BOX:
                case YELLOW_SHULKER_BOX:

                case FURNACE:
                case DROPPER:
                case DISPENSER:
                case ENCHANTING_TABLE:
                case BREWING_STAND:
                case CAULDRON:
                case HOPPER:
                case BEACON:
                case JUKEBOX:

                case ANVIL:
                case CHIPPED_ANVIL:
                case DAMAGED_ANVIL:
                    action = PermissableAction.CONTAINER;
                    break;
                default:
                    // Check for doors that might have diff material name in old version.
                    if (block.getType().name().contains("DOOR")) {
                        action = PermissableAction.DOOR;
                    }
                    break;
            }
        } else {
            if (block.getType().toString().toUpperCase().contains("BUTTON")) {
                action = PermissableAction.BUTTON;
            }

            switch (block.getType()) {
                case LEVER:
                    action = PermissableAction.LEVER;
                    break;
                case DARK_OAK_DOOR:
                case ACACIA_DOOR:
                case BIRCH_DOOR:
                case IRON_DOOR:
                case JUNGLE_DOOR:
                case SPRUCE_DOOR:
                case ACACIA_FENCE_GATE:
                case BIRCH_FENCE_GATE:
                case DARK_OAK_FENCE_GATE:
                case JUNGLE_FENCE_GATE:
                case SPRUCE_FENCE_GATE:
                    action = PermissableAction.DOOR;
                    break;
                case CHEST:
                case ENDER_CHEST:
                case TRAPPED_CHEST:
                case DISPENSER:
                case ENCHANTING_TABLE:
                case DROPPER:
                case FURNACE:
                case HOPPER:
                case ANVIL:
                case CHIPPED_ANVIL:
                case DAMAGED_ANVIL:
                case BREWING_STAND:
                    action = PermissableAction.CONTAINER;
                    break;
                default:
                    // Check for doors that might have diff material name in old version.
                    if (block.getType().name().contains("DOOR"))
                        action = PermissableAction.DOOR;
                    break;
            }
        }

        // We only care about some material types.
        if (otherFaction.hasPlayersOnline()) {
            if (!Conf.territoryProtectedMaterials.contains(material))
                return true;
        } else {
            if (!Conf.territoryProtectedMaterialsWhenOffline.contains(material))
                return true;
        }

        // Move up access check to check for exceptions
        Access access = otherFaction.getAccess(me, action);
        boolean doTerritoryEnemyProtectedCheck = true;

        if (action != null && (action.equals(PermissableAction.CONTAINER) ||
                action.equals(PermissableAction.DOOR))) {
            if (access == Access.ALLOW) {
                doTerritoryEnemyProtectedCheck = false;
            }
        }

        // Did not nest the boolean so that it stands out when Im looking
        // through the code later.
        if (doTerritoryEnemyProtectedCheck) {
            // You may use any block unless it is another faction's territory...
            if (rel.isNeutral() || (rel.isEnemy() && Conf.territoryEnemyProtectMaterials) || (rel.isAlly() && Conf.territoryAllyProtectMaterials) || (rel.isTruce() && Conf.territoryTruceProtectMaterials)) {
                if (!justCheck) {
                    me.msg(TL.PLAYER_USE_TERRITORY, (material == SavageFactionsPlugin.plugin.SOIL ? "trample " : "use ") + TextUtil.getMaterialName(material), otherFaction.getTag(myFaction));
                }
                return false;
            }
        }

        if (access != Access.ALLOW && me.getRole() != Role.LEADER) {
            // TODO: Update this once new access values are added other than just allow / deny.
            if ((myFaction.getOwnerListString(loc) != null && !myFaction.getOwnerListString(loc).isEmpty() && myFaction.getOwnerListString(loc).contains(player.getName()))) {
                return true;
            } else if (myFaction.getOwnerListString(loc) != null && !myFaction.getOwnerListString(loc).isEmpty() && !myFaction.getOwnerListString(loc).contains(player.getName())) {
                me.msg("<b>You can't " + action + " in this territory, it is owned by: " + myFaction.getOwnerListString(loc));
                return false;
            } else if (access == Access.DENY) {
                me.msg(TL.GENERIC_NOPERMISSION, action);
                return false;
            }
        }
        return true;
    }

    public static boolean preventCommand(String fullCmd, Player player) {
        if ((Conf.territoryNeutralDenyCommands.isEmpty() && Conf.territoryEnemyDenyCommands.isEmpty() && Conf.permanentFactionMemberDenyCommands.isEmpty() && Conf.warzoneDenyCommands.isEmpty())) {
            return false;
        }

        fullCmd = fullCmd.toLowerCase();

        FPlayer me = FPlayers.getInstance().getByPlayer(player);

        String shortCmd;  // command without the slash at the beginning
        if (fullCmd.startsWith("/")) {
            shortCmd = fullCmd.substring(1);
        } else {
            shortCmd = fullCmd;
            fullCmd = "/" + fullCmd;
        }

        if (me.hasFaction() &&
                !me.isAdminBypassing() &&
                !Conf.permanentFactionMemberDenyCommands.isEmpty() &&
                me.getFaction().isPermanent() &&
                isCommandInList(fullCmd, shortCmd, Conf.permanentFactionMemberDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_PERMANENT, fullCmd);
            return true;
        }

        Faction at = Board.getInstance().getFactionAt(new FLocation(player.getLocation()));

        if (at.isWilderness() && !Conf.wildernessDenyCommands.isEmpty() && !me.isAdminBypassing() && isCommandInList(fullCmd, shortCmd, Conf.wildernessDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_WILDERNESS, fullCmd);
            return true;
        }

        Relation rel = at.getRelationTo(me);
        if (at.isNormal() && rel.isAlly() && !Conf.territoryAllyDenyCommands.isEmpty() && !me.isAdminBypassing() && isCommandInList(fullCmd, shortCmd, Conf.territoryAllyDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_ALLY, fullCmd);
            return false;
        }

        if (at.isNormal() && rel.isNeutral() && !Conf.territoryNeutralDenyCommands.isEmpty() && !me.isAdminBypassing() && isCommandInList(fullCmd, shortCmd, Conf.territoryNeutralDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_NEUTRAL, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isEnemy() && !Conf.territoryEnemyDenyCommands.isEmpty() && !me.isAdminBypassing() && isCommandInList(fullCmd, shortCmd, Conf.territoryEnemyDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_ENEMY, fullCmd);
            return true;
        }

        if (at.isWarZone() && !Conf.warzoneDenyCommands.isEmpty() && !me.isAdminBypassing() && isCommandInList(fullCmd, shortCmd, Conf.warzoneDenyCommands.iterator())) {
            me.msg(TL.PLAYER_COMMAND_WARZONE, fullCmd);
            return true;
        }

        return false;
    }

    private static boolean isCommandInList(String fullCmd, String shortCmd, Iterator<String> iter) {
        String cmdCheck;
        while (iter.hasNext()) {
            cmdCheck = iter.next();
            if (cmdCheck == null) {
                iter.remove();
                continue;
            }

            cmdCheck = cmdCheck.toLowerCase();
            if (fullCmd.startsWith(cmdCheck) || shortCmd.startsWith(cmdCheck)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        initPlayer(event.getPlayer());
    }

    private void initPlayer(Player player) {
        // Make sure that all online players do have a fplayer.
        final FPlayer me = FPlayers.getInstance().getByPlayer(player);
        ((MemoryFPlayer) me).setName(player.getName());

        // Update the lastLoginTime for this fplayer
        me.setLastLoginTime(System.currentTimeMillis());

        // Store player's current FLocation and notify them where they are
        me.setLastStoodAt(new FLocation(player.getLocation()));

        me.login(); // set kills / deaths

        // Check for Faction announcements. Let's delay this so they actually see it.
        Faction myFaction = me.getFaction();
        Bukkit.getScheduler().runTaskLater(SavageFactionsPlugin.plugin, () -> {
            if (me.isOnline()) {
                myFaction.sendUnreadAnnouncements(me);
            }
        }, 33L); // Don't ask me why.

        if (SavageFactionsPlugin.plugin.getConfig().getBoolean("scoreboard.default-enabled", false)) {
            FScoreboard.init(me);
            FScoreboard.get(me).setDefaultSidebar(new FDefaultSidebar(), SavageFactionsPlugin.plugin.getConfig().getInt("scoreboard.default-update-interval", 20));
            FScoreboard.get(me).setSidebarVisibility(me.showScoreboard());
        }

        if (!myFaction.isWilderness()) {
            for (FPlayer other : myFaction.getFPlayersWhereOnline(true)) {
                if (other != me && other.isMonitoringJoins()) {
                    other.msg(TL.FACTION_LOGIN, me.getName());
                }
            }
        }

        fallMap.put(me.getPlayer(), false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(SavageFactionsPlugin.plugin, () ->
                fallMap.remove(me.getPlayer()), 180L);

        if (me.isSpyingChat() && !player.hasPermission(Permission.CHATSPY.node)) {
            me.setSpyingChat(false);
            SavageFactionsPlugin.plugin.log(Level.INFO, "Found %s spying chat without permission on login. Disabled their chat spying.", player.getName());
        }

        if (me.isAdminBypassing() && !player.hasPermission(Permission.BYPASS.node)) {
            me.setIsAdminBypassing(false);
            SavageFactionsPlugin.plugin.log(Level.INFO, "Found %s on admin Bypass without permission on login. Disabled it for them.", player.getName());
        }

        // If they have the permission, don't let them autoleave. Bad inverted setter :\
        me.setAutoLeave(!player.hasPermission(Permission.AUTO_LEAVE_BYPASS.node));
        me.setTakeFallDamage(true);
    }

    @EventHandler
    public void entityDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (fallMap.containsKey(player)) {
            event.setCancelled(true);
            fallMap.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        FPlayer me = FPlayers.getInstance().getByPlayer(event.getPlayer());

        // Make sure player's power is up to date when they log off.
        me.getPower();
        // and update their last login time to point to when the logged off, for auto-remove routine
        me.setLastLoginTime(System.currentTimeMillis());

        me.logout(); // cache kills / deaths

        // if player is waiting for fstuck teleport but leaves, remove
        if (SavageFactionsPlugin.plugin.getStuckMap().containsKey(me.getPlayer().getUniqueId())) {
            FPlayers.getInstance().getByPlayer(me.getPlayer()).msg(TL.COMMAND_STUCK_CANCELLED);
            SavageFactionsPlugin.plugin.getStuckMap().remove(me.getPlayer().getUniqueId());
            SavageFactionsPlugin.plugin.getTimers().remove(me.getPlayer().getUniqueId());
        }

        Faction myFaction = me.getFaction();
        if (!myFaction.isWilderness()) {
            myFaction.memberLoggedOff();
        }

        if (!myFaction.isWilderness()) {
            for (FPlayer player : myFaction.getFPlayersWhereOnline(true)) {
                if (player != me && player.isMonitoringJoins()) {
                    player.msg(TL.FACTION_LOGOUT, me.getName());
                }
            }
        }

        CmdSeeChunk.seeChunkMap.remove(event.getPlayer().getName());

        FScoreboard.remove(me);
    }

    public String parseAllPlaceholders(String string, Faction faction, Player player) {
        string = TagUtil.parsePlaceholders(player, string);

        string = string.replace("{Faction}", faction.getTag())
                .replace("{online}", faction.getOnlinePlayers().size() + "")
                .replace("{offline}", faction.getFPlayers().size() - faction.getOnlinePlayers().size() + "")
                .replace("{chunks}", faction.getAllClaims().size() + "")
                .replace("{power}", faction.getPower() + "")
                .replace("{leader}", faction.getFPlayerAdmin() + "");

        return string;
    }

    public void enableFly(FPlayer me) {
        if (SavageFactionsPlugin.plugin.getConfig().getBoolean("ffly.AutoEnable")) {

            me.setFlying(true);
            CmdFly.flyMap.put(me.getName(), true);
            if (CmdFly.id == -1) {
                if (SavageFactionsPlugin.plugin.getConfig().getBoolean("ffly.Particles.Enabled")) {
                    CmdFly.startParticles();
                }
            }
            if (CmdFly.flyid == -1) {
                CmdFly.startFlyCheck();
            }
        }
    }

    //inspect
    @EventHandler
    public void onInspect(PlayerInteractEvent e) {
        if (e.getAction().name().contains("BLOCK")) {
            FPlayer fplayer = FPlayers.getInstance().getByPlayer(e.getPlayer());
            if (!fplayer.isInspectMode()) {
                return;
            }
            e.setCancelled(true);
            if (!fplayer.isAdminBypassing()) {
                if (!fplayer.hasFaction()) {
                    fplayer.setInspectMode(false);
                    fplayer.msg(TL.COMMAND_INSPECT_DISABLED_NOFAC);
                    return;
                }
                if (fplayer.getFaction() != Board.getInstance().getFactionAt(new FLocation(e.getPlayer().getLocation()))) {
                    fplayer.msg(TL.COMMAND_INSPECT_NOTINCLAIM);
                    return;
                }
            } else {
                fplayer.msg(TL.COMMAND_INSPECT_BYPASS);
            }
            List<String[]> info = CoreProtect.getInstance().getAPI().blockLookup(e.getClickedBlock(), 0);
            if (info.size() == 0) {
                e.getPlayer().sendMessage(TL.COMMAND_INSPECT_NODATA.toString());
                return;
            }
            Player player = e.getPlayer();
            CoreProtectAPI coAPI = CoreProtect.getInstance().getAPI();
            player.sendMessage(TL.COMMAND_INSPECT_HEADER.toString().replace("{x}", e.getClickedBlock().getX() + "")
                    .replace("{y}", e.getClickedBlock().getY() + "")
                    .replace("{z}", e.getClickedBlock().getZ() + ""));
            String rowFormat = TL.COMMAND_INSPECT_ROW.toString();
            for (int i = 0; i < info.size(); i++) {
                CoreProtectAPI.ParseResult row = coAPI.parseResult(info.get(0));
                player.sendMessage(rowFormat
                        .replace("{time}", convertTime(row.getTime()))
                        .replace("{action}", row.getActionString())
                        .replace("{player}", row.getPlayer())
                        .replace("{block-type}", row.getType().toString().toLowerCase()));
            }
        }
    }

    //For disabling enderpearl throws
    @EventHandler
    public void onPearl(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (player.getItemInHand().getType() != Material.ENDER_PEARL) {
            return;
        }

        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        if (fPlayer.isFlying() && Conf.noEnderpearlsInFly) {
            fPlayer.msg(TL.COMMAND_FLY_NO_EPEARL);
            e.setCancelled(true);
        }
    }

    //For Blocking Homes and Blocking Teleportation To Homes
    @EventHandler
    public void onPlayerHomeCheck(PlayerTeleportEvent event) throws Exception {
        if (event.getPlayer().hasMetadata("NPC")) {
            return;
        }
        if (event.getPlayer().hasPermission("factions.homes.bypass")) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("Essentials") == null) {
            return;
        }
        boolean isHome = false;
        for (String str : SavageFactionsPlugin.plugin.ess.getUser(event.getPlayer()).getHomes()) {
            Location home = SavageFactionsPlugin.plugin.ess.getUser(event.getPlayer()).getHome(str);
            if (home.getBlockX() == event.getTo().getBlockX() && home.getBlockY() == event.getTo().getBlockY() && home.getBlockZ() == event.getTo().getBlockZ()) {
                isHome = true;
            }
        }
        if (!isHome) {
            return;
        }
        Location loc = event.getTo();
        FLocation floc = new FLocation(event.getTo());
        Faction fac = Board.getInstance().getFactionAt(floc);
        Player player = event.getPlayer();
        FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
        User user = SavageFactionsPlugin.plugin.ess.getUser(event.getPlayer());
        List<String> homes = user.getHomes();
        if (fac.isWilderness() || FPlayers.getInstance().getByPlayer(event.getPlayer()).getFactionId().equals(fac.getUniqueId())) {
            return;
        }
        //Warzone and SafeZone Home Initializers
        if (fac.isWarZone() || fac.isSafeZone() && SavageFactionsPlugin.plugin.getConfig().getBoolean("deny-homes-in-system-factions")) {
            event.setCancelled(true);
            fplayer.msg(TL.COMMAND_HOME_BLOCKED, fac.getTag());
            if (SavageFactionsPlugin.plugin.getConfig().getBoolean("remove-homes-in-system-factions"))
                for (String s : homes) {
                    if (user.getHome(s).getBlock().getLocation().getChunk().equals(loc.getChunk())) {
                        user.delHome(s);
                    }
                }
        }

        if (fplayer.getFaction().getRelationTo(fac) == Relation.ENEMY && SavageFactionsPlugin.plugin.getConfig().getBoolean("deny-homes-in-enemy-factions")) {
            event.setCancelled(true);
            fplayer.msg(TL.COMMAND_HOME_BLOCKED, fac.getTag());
            if (SavageFactionsPlugin.plugin.getConfig().getBoolean("remove-homes-in-enemy-factions"))
                for (String s : homes) {
                    if (user.getHome(s).getBlock().getLocation().getChunk().equals(loc.getChunk())) {
                        user.delHome(s);
                    }
                }
        }
        if (fplayer.getFaction().getRelationTo(fac) == Relation.NEUTRAL && SavageFactionsPlugin.plugin.getConfig().getBoolean("deny-homes-in-neutral-factions")) {
            event.setCancelled(true);
            fplayer.msg(TL.COMMAND_HOME_BLOCKED, fac.getTag());
            if (SavageFactionsPlugin.plugin.getConfig().getBoolean("remove-homes-in-neutral-factions"))
                for (String s : homes) {
                    if (user.getHome(s).getBlock().getLocation().getChunk().equals(loc.getChunk())) {
                        user.delHome(s);
                    }
                }
        }
        if (fplayer.getFaction().getRelationTo(fac) == Relation.ALLY && SavageFactionsPlugin.plugin.getConfig().getBoolean("deny-homes-in-ally-factions")) {
            event.setCancelled(true);
            fplayer.msg(TL.COMMAND_HOME_BLOCKED, fac.getTag());
            if (SavageFactionsPlugin.plugin.getConfig().getBoolean("remove-homes-in-ally-factions"))
                for (String s : homes) {
                    if (user.getHome(s).getBlock().getLocation().getChunk().equals(loc.getChunk())) {
                        user.delHome(s);
                    }
                }
        }
        if (fplayer.getFaction().getRelationTo(fac) == Relation.TRUCE && SavageFactionsPlugin.plugin.getConfig().getBoolean("deny-homes-in-truce-factions")) {
            event.setCancelled(true);
            fplayer.msg(TL.COMMAND_HOME_BLOCKED, fac.getTag());
            if (SavageFactionsPlugin.plugin.getConfig().getBoolean("remove-homes-in-truce-factions"))
                for (String s : homes) {
                    if (user.getHome(s).getBlock().getLocation().getChunk().equals(loc.getChunk())) {
                        user.delHome(s);
                    }
                }
        }
    }

    private String convertTime(int time) {
        String result = String.valueOf(Math.round((System.currentTimeMillis() / 1000L - time) / 36.0D) / 100.0D);
        return (result.length() == 3 ? result + "0" : result) + "/hrs ago";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        FPlayer me = FPlayers.getInstance().getByPlayer(player);

        // clear visualization
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            VisualizeUtil.clear(event.getPlayer());
            if (me.isWarmingUp()) {
                me.clearWarmup();
                me.msg(TL.WARMUPS_CANCELLED);
            }
        }

        // quick check to make sure player is moving between chunks; good performance boost
        if (event.getFrom().getBlockX() >> 4 == event.getTo().getBlockX() >> 4 && event.getFrom().getBlockZ() >> 4 == event.getTo().getBlockZ() >> 4 && event.getFrom().getWorld() == event.getTo().getWorld()) {
            return;
        }


        // Did we change coord?
        FLocation from = me.getLastStoodAt();
        FLocation to = new FLocation(event.getTo());

        if (from.equals(to)) {
            return;
        }

        // Yes we did change coord (:

        me.setLastStoodAt(to);

        // Did we change "host"(faction)?
        Faction factionFrom = Board.getInstance().getFactionAt(from);
        Faction factionTo = Board.getInstance().getFactionAt(to);
        boolean changedFaction = (factionFrom != factionTo);


        if (changedFaction) {
            Bukkit.getServer().getPluginManager().callEvent(new FPlayerEnteredFactionEvent(factionTo, factionFrom, me));
            if (SavageFactionsPlugin.plugin.getConfig().getBoolean("Title.Show-Title")) {
                String title = SavageFactionsPlugin.plugin.getConfig().getString("Title.Format.Title");
                title = title.replace("{Faction}", factionTo.getColorTo(me) + factionTo.getTag());
                title = parseAllPlaceholders(title, factionTo, player);
                String subTitle = SavageFactionsPlugin.plugin.getConfig().getString("Title.Format.Subtitle").replace("{Description}", factionTo.getDescription()).replace("{Faction}", factionTo.getColorTo(me) + factionTo.getTag());
                subTitle = parseAllPlaceholders(subTitle, factionTo, player);
                if (SavageFactionsPlugin.plugin.serverVersion != ServerVersion.MC_V17) {
                    if (SavageFactionsPlugin.plugin.serverVersion != ServerVersion.MC_V18) {
                        me.getPlayer().sendTitle(SavageFactionsPlugin.plugin.color(title), SavageFactionsPlugin.plugin.color(subTitle), SavageFactionsPlugin.plugin.getConfig().getInt("Title.Options.FadeInTime"),
                                SavageFactionsPlugin.plugin.getConfig().getInt("Title.Options.ShowTime"),
                                SavageFactionsPlugin.plugin.getConfig().getInt("Title.Options.FadeOutTime"));
                    } else {
                        me.getPlayer().sendTitle(SavageFactionsPlugin.plugin.color(title), SavageFactionsPlugin.plugin.color(subTitle));
                    }
                }
            }

            if (!SavageFactionsPlugin.plugin.factionsFlight) {
                return;
            }

            // enable fly :)
            if (me.hasFaction() && !me.isFlying()) {
                if (factionTo == me.getFaction()) {
                    enableFly(me);
                }
                // bypass checks
                Relation relationTo = factionTo.getRelationTo(me);
                if ((factionTo.isWilderness() && me.canflyinWilderness()) ||
                        (factionTo.isWarZone() && me.canflyinWarzone()) ||
                        (factionTo.isSafeZone() && me.canflyinSafezone()) ||
                        (relationTo == Relation.ENEMY && me.canflyinEnemy()) ||
                        (relationTo == Relation.ALLY && me.canflyinAlly()) ||
                        (relationTo == Relation.TRUCE && me.canflyinTruce()) ||
                        (relationTo == Relation.NEUTRAL && me.canflyinNeutral() && !isSystemFaction(factionTo))) {
                    enableFly(me);
                }

            }
        }

        if (me.isMapAutoUpdating()) {
            if (showTimes.containsKey(player.getUniqueId()) && (showTimes.get(player.getUniqueId()) > System.currentTimeMillis())) {
                if (SavageFactionsPlugin.plugin.getConfig().getBoolean("findfactionsexploit.log", false)) {
                    SavageFactionsPlugin.plugin.log(Level.WARNING, "%s tried to show a faction map too soon and triggered exploit blocker.", player.getName());
                }
            } else {
                me.sendFancyMessage(Board.getInstance().getMap(me, to, player.getLocation().getYaw()));
                showTimes.put(player.getUniqueId(), System.currentTimeMillis() + SavageFactionsPlugin.plugin.getConfig().getLong("findfactionsexploit.cooldown", 2000));
            }
        } else {
            Faction myFaction = me.getFaction();
            String ownersTo = myFaction.getOwnerListString(to);
            if (changedFaction) {
                me.sendFactionHereMessage(factionFrom);
                if (Conf.ownedAreasEnabled && Conf.ownedMessageOnBorder && myFaction == factionTo && !ownersTo.isEmpty()) {
                    me.sendMessage(TL.GENERIC_OWNERS.format(ownersTo));
                }
            } else if (Conf.ownedAreasEnabled && Conf.ownedMessageInsideTerritory && myFaction == factionTo && !myFaction.isWilderness()) {
                String ownersFrom = myFaction.getOwnerListString(from);
                if (Conf.ownedMessageByChunk || !ownersFrom.equals(ownersTo)) {
                    if (!ownersTo.isEmpty()) {
                        me.sendMessage(TL.GENERIC_OWNERS.format(ownersTo));
                    } else if (!TL.GENERIC_PUBLICLAND.toString().isEmpty()) {
                        me.sendMessage(TL.GENERIC_PUBLICLAND.toString());
                    }
                }
            }
        }

        if (me.getAutoClaimFor() != null) {
            me.attemptClaim(me.getAutoClaimFor(), event.getTo(), true);
        } else if (me.isAutoSafeClaimEnabled()) {
            if (!Permission.MANAGE_SAFE_ZONE.has(player)) {
                me.setIsAutoSafeClaimEnabled(false);
            } else {
                if (!Board.getInstance().getFactionAt(to).isSafeZone()) {
                    Board.getInstance().setFactionAt(Factions.getInstance().getSafeZone(), to);
                    me.msg(TL.PLAYER_SAFEAUTO);
                }
            }
        } else if (me.isAutoWarClaimEnabled()) {
            if (!Permission.MANAGE_WAR_ZONE.has(player)) {
                me.setIsAutoWarClaimEnabled(false);
            } else {
                if (!Board.getInstance().getFactionAt(to).isWarZone()) {
                    Board.getInstance().setFactionAt(Factions.getInstance().getWarZone(), to);
                    me.msg(TL.PLAYER_WARAUTO);
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        FPlayer fPlayer = FPlayers.getInstance().getById(e.getPlayer().getUniqueId());

        if (fPlayer.isInVault())
            fPlayer.setInVault(false);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        /// Prevents the use of monster eggs in owned land.
        /*if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.hasItem() || event.hasBlock()) {
                ItemStack itemStack = event.getItem();
                if (itemStack.getType() == Material.MONSTER_EGG) {
                    FLocation loc = new FLocation(event.getClickedBlock().getLocation());
                    Faction faction = Board.getInstance().getFactionAt(loc);
                    FPlayer me = FPlayers.getInstance().getByPlayer(event.getPlayer());
                    if (Conf.ownedAreasEnabled && !faction.playerHasOwnershipRights(me, loc)) {
                        if (Conf.ownedAreaDenyBuild) {
                            me.msg("<b>You can't use spawn eggs in this territory, it is owned by: " + faction.getOwnerListString(loc));
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }*/
        // only need to check right-clicks and physical as of MC 1.4+; good performance boost
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL)
            return;

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (block == null)
            return;  // clicked in air, apparently

        if (!canPlayerUseBlock(player, block, false)) {
            event.setCancelled(true);
            if (Conf.handleExploitInteractionSpam) {
                String name = player.getName();
                InteractAttemptSpam attempt = interactSpammers.get(name);
                if (attempt == null) {
                    attempt = new InteractAttemptSpam();
                    interactSpammers.put(name, attempt);
                }

                int count = attempt.increment();
                if (count >= 10) {
                    FPlayer me = FPlayers.getInstance().getByPlayer(player);
                    me.msg(TL.PLAYER_OUCH);
                    player.damage(NumberConversions.floor((double) count / 10));
                }
            }
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;  // only interested on right-clicks for below
        }

        if (!playerCanUseItemHere(player, block.getLocation(), event.getMaterial(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBoneMeal(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == MultiversionMaterials.GRASS_BLOCK.parseMaterial()
                && event.hasItem() && event.getItem().getType() == MultiversionMaterials.BONE_MEAL.parseMaterial()) {
            if (!FactionsBlockListener.playerCanBuildDestroyBlock(event.getPlayer(), block.getLocation(), PermissableAction.BUILD.name(), true)) {
                FPlayer me = FPlayers.getInstance().getById(event.getPlayer().getUniqueId());
                Faction otherFaction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
                Faction myFaction = me.getFaction();

                me.msg("<b>You can't use bone meal in the territory of " + otherFaction.getTag(myFaction));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(event.getPlayer());
        fPlayer.updatePower();  // update power, so they won't have gained any while dead

        Location home = fPlayer.getFaction().getHome();
        if (Conf.homesEnabled &&
                Conf.homesTeleportToOnDeath &&
                home != null &&
                (Conf.homesRespawnFromNoPowerLossWorlds || !Conf.worldsNoPowerLoss.contains(event.getPlayer().getWorld().getName()))) {
            event.setRespawnLocation(home);
        }
    }

    // For some reason onPlayerInteract() sometimes misses bucket events depending on distance (something like 2-3 blocks away isn't detected),
    // but these separate bucket events below always fire without fail
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Block block = event.getBlockClicked();
        Player player = event.getPlayer();

        if (!playerCanUseItemHere(player, block.getLocation(), event.getBucket(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Block block = event.getBlockClicked();
        Player player = event.getPlayer();

        if (!playerCanUseItemHere(player, block.getLocation(), event.getBucket(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(event.getPlayer());

        if (fPlayer == null) {
            return;
        }

        // if player was banned (not just kicked), get rid of their stored info
        if (!Conf.removePlayerDataWhenBanned && !event.getReason().equals("Banned by admin.")) {
            return;
        }

        if (fPlayer.getRole() == Role.LEADER) {
            fPlayer.getFaction().promoteNewLeader();
        }

        fPlayer.leave(false);
        fPlayer.remove();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    final public void onFactionJoin(FPlayerJoinEvent event) {
        FTeamWrapper.applyUpdatesLater(event.getFaction());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFactionLeave(FPlayerLeaveEvent event) {
        FTeamWrapper.applyUpdatesLater(event.getFaction());
    }

    private static class InteractAttemptSpam {
        private int attempts = 0;
        private long lastAttempt = System.currentTimeMillis();

        // returns the current attempt count
        public int increment() {
            long now = System.currentTimeMillis();
            if (now > lastAttempt + 2000) {
                attempts = 1;
            } else {
                attempts++;
            }
            lastAttempt = now;
            return attempts;
        }
    }
}
