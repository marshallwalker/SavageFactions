package com.massivecraft.factions.listeners;

import com.massivecraft.factions.*;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradeConfiguration;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradeLevel;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradesConfiguration;
import com.massivecraft.factions.event.PowerLossEvent;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Upgrade;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;


public class FactionsEntityListener implements Listener {

    private static final Set<PotionEffectType> badPotionEffects = new LinkedHashSet<>(Arrays.asList(PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM, PotionEffectType.HUNGER, PotionEffectType.POISON, PotionEffectType.SLOW, PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.WITHER));

    private final SavageFactionsPlugin plugin;

    public FactionsEntityListener(SavageFactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        //handleExpUpgrade(event);

        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = Board.getInstance().getFactionAt(new FLocation(player.getLocation()));

        PowerLossEvent powerLossEvent = new PowerLossEvent(faction, fplayer);
        // Check for no power loss conditions
        if (faction.isWarZone()) {
            // war zones always override worldsNoPowerLoss either way, thus this layout
            if (!Conf.warZonePowerLoss) {
                powerLossEvent.setMessage(TL.PLAYER_POWER_NOLOSS_WARZONE.toString());
                powerLossEvent.setCancelled(true);
            }
            if (Conf.worldsNoPowerLoss.contains(player.getWorld().getName())) {
                powerLossEvent.setMessage(TL.PLAYER_POWER_LOSS_WARZONE.toString());
            }
        } else if (faction.isWilderness() && !Conf.wildernessPowerLoss && !Conf.worldsNoWildernessProtection.contains(player.getWorld().getName())) {
            powerLossEvent.setMessage(TL.PLAYER_POWER_NOLOSS_WILDERNESS.toString());
            powerLossEvent.setCancelled(true);
        } else if (Conf.worldsNoPowerLoss.contains(player.getWorld().getName())) {
            powerLossEvent.setMessage(TL.PLAYER_POWER_NOLOSS_WORLD.toString());
            powerLossEvent.setCancelled(true);
        } else if (Conf.peacefulMembersDisablePowerLoss && fplayer.hasFaction() && fplayer.getFaction().isPeaceful()) {
            powerLossEvent.setMessage(TL.PLAYER_POWER_NOLOSS_PEACEFUL.toString());
            powerLossEvent.setCancelled(true);
        } else {
            powerLossEvent.setMessage(TL.PLAYER_POWER_NOW.toString());
        }

        // call Event
        Bukkit.getPluginManager().callEvent(powerLossEvent);

        // Call player onEntityDeath if the event is not cancelled
        if (!powerLossEvent.isCancelled()) {
            fplayer.onDeath();
        }
        // Send the message from the powerLossEvent
        final String msg = powerLossEvent.getMessage();
        if (msg != null && !msg.isEmpty()) {
            fplayer.msg(msg, fplayer.getPowerRounded(), fplayer.getPowerMaxRounded());
        }
    }

    //TODO remove?
//    ////////////////////////////////////////////////////
//    //  FACTION EXP UPGRADES
//    ////////////////////////////////////////////////////
//
//    private void handleExpUpgrade(EntityDeathEvent event) {
//        Entity killer = event.getEntity().getKiller();
//
//        if (killer == null)
//            return;
//
//        FLocation fLocation = new FLocation(event.getEntity().getLocation());
//        Faction factionAt = Board.getInstance().getFactionAt(fLocation);
//
//        if (factionAt.isWilderness()) {
//            return;
//        }
//
//        int level = factionAt.getUpgrade(Upgrade.EXP);
//
//        if (level < 0) {
//            return;
//        }
//
//        double multiplier = SavageFactionsPlugin.plugin.getConfig().getDouble("fupgrades.MainMenu.EXP.EXP-Boost.level-" + level);
//
//        if (multiplier >= 0) {
//            event.setDroppedExp((int) (event.getDroppedExp() * multiplier));
//        }
//    }

    /**
     * Who can I hurt? I can never hurt members or allies. I can always hurt enemies. I can hurt neutrals as long as
     * they are outside their own territory.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;
            if (!this.canDamagerHurtDamagee(sub, true)) {
                event.setCancelled(true);
            }
            // event is not cancelled by factions

            Entity damagee = sub.getEntity();
            Entity damager = sub.getDamager();
            if (damagee instanceof Player) {
                if (damager instanceof Player) {
                    FPlayer fdamager = FPlayers.getInstance().getByPlayer((Player) damager);
                    FPlayer fdamagee = FPlayers.getInstance().getByPlayer((Player) damagee);
                    if ((fdamagee.getRelationTo(fdamager) == Relation.ALLY) ||
                            (fdamagee.getRelationTo(fdamager) == Relation.TRUCE) ||
                            (fdamagee.getFaction() == fdamager.getFaction())) {
                        return;
                    }
                } else {

                    // this triggers if damagee is a player and damager is  mob ( so like if a skeleton hits u )
                    if (damager instanceof Projectile) {
                        // this will trigger if the damager is a projectile
                        if (((Projectile) damager).getShooter() instanceof Player) {
                            Player damagerPlayer = (Player) ((Projectile) damager).getShooter();
                            FPlayer fdamager = FPlayers.getInstance().getByPlayer(damagerPlayer);
                            FPlayer fdamagee = FPlayers.getInstance().getByPlayer((Player) damagee);
                            Relation relation = fdamager.getRelationTo(fdamagee);
                            if (relation == Relation.ALLY || relation == Relation.TRUCE ||
                                    fdamager.getFaction() == fdamagee.getFaction()) {
                                // this should disable the fly so
                                return;
                            }
                        } else {
                            // this should trigger if the attacker shootin the arrow is a mob
                            return;
                        }

                    }
                }
            } else {
                // Protect armor stands/item frames from being damaged in protected territories
                if (damagee.getType() == EntityType.ITEM_FRAME || damagee.getType() == EntityType.ARMOR_STAND) {
                    // Manage projectiles launched by players
                    if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Entity) {
                        damager = (Entity) ((Projectile) damager).getShooter();
                    }

                    // Run the check for a player
                    if (damager instanceof Player) {
                        // Generate the action message.
                        String entityAction;

                        if (damagee.getType() == EntityType.ITEM_FRAME) {
                            entityAction = "item frames";
                        } else {
                            entityAction = "armor stands";
                        }

                        if (!FactionsBlockListener.playerCanBuildDestroyBlock((Player) damager, damagee.getLocation(), "destroy " + entityAction, false)) {
                            event.setCancelled(true);
                        }
                    } else {
                        // we don't want to let mobs/arrows destroy item frames/armor stands
                        // so we only have to run the check as if there had been an explosion at the damager location
                        if (!this.checkExplosionForBlock(damager, damagee.getLocation().getBlock())) {
                            event.setCancelled(true);
                        }
                    }

                    // we don't need to go after
                    return;
                }

                //this one should trigger if something other than a player takes damage
                if (damager instanceof Player) {
                    // now itll only go here if the damage is dealt by a player
                    return;
                    // we cancel it so fly isnt removed when you hit a mob etc
                }
            }
            if (damagee != null && damagee instanceof Player) {
                cancelFStuckTeleport((Player) damagee);
                cancelFFly((Player) damagee);
                FPlayer fplayer = FPlayers.getInstance().getByPlayer((Player) damagee);
                if (fplayer.isInspectMode()) {
                    fplayer.setInspectMode(false);
                    fplayer.msg(TL.COMMAND_INSPECT_DISABLED_MSG);
                }
            }
            if (damager instanceof Player) {
                cancelFStuckTeleport((Player) damager);
                cancelFFly((Player) damager);
                FPlayer fplayer = FPlayers.getInstance().getByPlayer((Player) damager);
                if (fplayer.isInspectMode()) {
                    fplayer.setInspectMode(false);
                    fplayer.msg(TL.COMMAND_INSPECT_DISABLED_MSG);
                }
            }
        } else if (Conf.safeZonePreventAllDamageToPlayers && isPlayerInSafeZone(event.getEntity())) {
            // Players can not take any damage in a Safe Zone
            event.setCancelled(true);
        } else if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
            if (fPlayer != null && !fPlayer.shouldTakeFallDamage()) {
                event.setCancelled(true); // Falling after /f fly
            }
        }

        // entity took generic damage?
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            FPlayer me = FPlayers.getInstance().getByPlayer(player);
            cancelFStuckTeleport(player);
            if (me.isWarmingUp()) {
                me.clearWarmup();
                me.msg(TL.WARMUPS_CANCELLED);
            }
        }
    }

    private void cancelFFly(Player player) {
        if (player == null) {
            return;
        }

        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if (fPlayer.isFlying()) {
            fPlayer.setFFlying(false, true);
        }
    }

    public void cancelFStuckTeleport(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (SavageFactionsPlugin.plugin.getStuckMap().containsKey(uuid)) {
            FPlayers.getInstance().getByPlayer(player).msg(TL.COMMAND_STUCK_CANCELLED);
            SavageFactionsPlugin.plugin.getStuckMap().remove(uuid);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity boomer = event.getEntity();

        // Before we need to check the location where the block is placed
        if (!this.checkExplosionForBlock(boomer, event.getLocation().getBlock())) {
            event.setCancelled(true);
            return;
        }

        // Loop the blocklist to run checks on each aimed block
        Iterator<Block> blockList = event.blockList().iterator();

        while (blockList.hasNext()) {
            Block block = blockList.next();

            if (!this.checkExplosionForBlock(boomer, block)) {
                // The block don't have to explode
                blockList.remove();
            }
        }

        // Cancel the event if no block will explode
        if (event.blockList().isEmpty()) {
            event.setCancelled(true);

            // Or handle the exploit of TNT in water/lava
        } else if ((boomer instanceof TNTPrimed || boomer instanceof ExplosiveMinecart) && Conf.handleExploitTNTWaterlog) {
            // TNT in water/lava doesn't normally destroy any surrounding blocks, which is usually desired behavior, but...
            // this change below provides workaround for waterwalling providing perfect protection,
            // and makes cheap (non-obsidian) TNT cannons require minor maintenance between shots
            Block center = event.getLocation().getBlock();

            if (center.isLiquid()) {
                // a single surrounding block in all 6 directions is broken if the material is weak enough
                List<Block> targets = new ArrayList<>();
                targets.add(center.getRelative(0, 0, 1));
                targets.add(center.getRelative(0, 0, -1));
                targets.add(center.getRelative(0, 1, 0));
                targets.add(center.getRelative(0, -1, 0));
                targets.add(center.getRelative(1, 0, 0));
                targets.add(center.getRelative(-1, 0, 0));

                for (Block target : targets) {
                    @SuppressWarnings("deprecation")
                    int id = target.getType().getId();
                    // ignore air, bedrock, water, lava, obsidian, enchanting table, etc.... too bad we can't get a blast resistance value through Bukkit yet
                    if (id != 0 && (id < 7 || id > 11) && id != 49 && id != 90 && id != 116 && id != 119 && id != 120 && id != 130) {
                        target.breakNaturally();
                    }
                }
            }
        }
    }

    private boolean checkExplosionForBlock(Entity boomer, Block block) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));

        if (faction.noExplosionsInTerritory() || (faction.isPeaceful() && Conf.peacefulTerritoryDisableBoom)) {
            // faction is peaceful and has explosions set to disabled
            return false;
        }

        boolean online = faction.hasPlayersOnline();

        if (boomer instanceof Creeper && ((faction.isWilderness() && Conf.wildernessBlockCreepers && !Conf.worldsNoWildernessProtection.contains(block.getWorld().getName())) ||
                (faction.isNormal() && (online ? Conf.territoryBlockCreepers : Conf.territoryBlockCreepersWhenOffline)) ||
                (faction.isWarZone() && Conf.warZoneBlockCreepers) ||
                faction.isSafeZone())) {
            // creeper which needs prevention
            return false;
        } else if (
            // it's a bit crude just using fireball protection for Wither boss too, but I'd rather not add in a whole new set of xxxBlockWitherExplosion or whatever
                (boomer instanceof Fireball || boomer instanceof WitherSkull || boomer instanceof Wither) && ((faction.isWilderness() && Conf.wildernessBlockFireballs && !Conf.worldsNoWildernessProtection.contains(block.getWorld().getName())) ||
                        (faction.isNormal() && (online ? Conf.territoryBlockFireballs : Conf.territoryBlockFireballsWhenOffline)) ||
                        (faction.isWarZone() && Conf.warZoneBlockFireballs) ||
                        faction.isSafeZone())) {
            // ghast fireball which needs prevention
            return false;
        } else
            return (!(boomer instanceof TNTPrimed) && !(boomer instanceof ExplosiveMinecart)) || ((!faction.isWilderness() || !Conf.wildernessBlockTNT || Conf.worldsNoWildernessProtection.contains(block.getWorld().getName())) &&
                    (!faction.isNormal() || (online ? !Conf.territoryBlockTNT : !Conf.territoryBlockTNTWhenOffline)) &&
                    (!faction.isWarZone() || !Conf.warZoneBlockTNT) &&
                    (!faction.isSafeZone() || !Conf.safeZoneBlockTNT));

        // No condition retained, destroy the block!
    }

    // mainly for flaming arrows; don't want allies or people in safe zones to be ignited even after damage event is cancelled
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityCombustByEntity(EntityCombustByEntityEvent event) {
        EntityDamageByEntityEvent sub = new EntityDamageByEntityEvent(event.getCombuster(), event.getEntity(), EntityDamageEvent.DamageCause.FIRE, 0d);
        if (!this.canDamagerHurtDamagee(sub, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotionSplashEvent(PotionSplashEvent event) {
        // see if the potion has a harmful effect
        boolean badjuju = false;
        for (PotionEffect effect : event.getPotion().getEffects()) {
            if (badPotionEffects.contains(effect.getType())) {
                badjuju = true;
                break;
            }
        }
        if (!badjuju) {
            return;
        }

        ProjectileSource thrower = event.getPotion().getShooter();
        if (!(thrower instanceof Entity)) {
            return;
        }

        if (thrower instanceof Player) {
            Player player = (Player) thrower;
            FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
            if (badjuju && fPlayer.getFaction().isPeaceful()) {
                event.setCancelled(true);
                return;
            }
        }

        // scan through affected entities to make sure they're all valid targets
        for (LivingEntity target : event.getAffectedEntities()) {
            EntityDamageByEntityEvent sub = new EntityDamageByEntityEvent((Entity) thrower, target, EntityDamageEvent.DamageCause.CUSTOM, 0);
            if (!this.canDamagerHurtDamagee(sub, true)) {
                event.setIntensity(target, 0.0);  // affected entity list doesn't accept modification (so no iter.remove()), but this works
            }
        }
    }

    public boolean isPlayerInSafeZone(Entity damagee) {
        if (!(damagee instanceof Player)) {
            return false;
        }
        return Board.getInstance().getFactionAt(new FLocation(damagee.getLocation())).isSafeZone();
    }

    public boolean canDamagerHurtDamagee(EntityDamageByEntityEvent sub) {
        return canDamagerHurtDamagee(sub, true);
    }

    public boolean canDamagerHurtDamagee(EntityDamageByEntityEvent sub, boolean notify) {
        Entity damager = sub.getDamager();
        Entity damagee = sub.getEntity();

        if (!(damagee instanceof Player)) {
            return true;
        }

        FPlayer defender = FPlayers.getInstance().getByPlayer((Player) damagee);

        if (defender == null || defender.getPlayer() == null) {
            return true;
        }

        Location defenderLoc = defender.getPlayer().getLocation();
        Faction defLocFaction = Board.getInstance().getFactionAt(new FLocation(defenderLoc));

        // for damage caused by projectiles, getDamager() returns the projectile... what we need to know is the source
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;

            if (!(projectile.getShooter() instanceof Entity)) {
                return true;
            }

            damager = (Entity) projectile.getShooter();
        }

        if (damager == damagee)  // ender pearl usage and other self-inflicted damage
        {
            return true;
        }

        // Players can not take attack damage in a SafeZone, or possibly peaceful territory
        if (defLocFaction.noPvPInTerritory()) {
            if (damager instanceof Player) {
                if (notify) {
                    FPlayer attacker = FPlayers.getInstance().getByPlayer((Player) damager);
                    attacker.msg(TL.PLAYER_CANTHURT, (defLocFaction.isSafeZone() ? TL.REGION_SAFEZONE.toString() : TL.REGION_PEACEFUL.toString()));
                }
                return false;
            }
            return !defLocFaction.noMonstersInTerritory();
        }

        if (!(damager instanceof Player)) {
            return true;
        }

        FPlayer attacker = FPlayers.getInstance().getByPlayer((Player) damager);

        if (attacker == null || attacker.getPlayer() == null) {
            return true;
        }

        if (Conf.playersWhoBypassAllProtection.contains(attacker.getName())) {
            return true;
        }

        if (attacker.hasLoginPvpDisabled()) {
            if (notify) {
                attacker.msg(TL.PLAYER_PVP_LOGIN, Conf.noPVPDamageToOthersForXSecondsAfterLogin);
            }
            return false;
        }

        Faction locFaction = Board.getInstance().getFactionAt(new FLocation(attacker));

        // so we know from above that the defender isn't in a safezone... what about the attacker, sneaky dog that he might be?
        if (locFaction.noPvPInTerritory()) {
            if (notify) {
                attacker.msg(TL.PLAYER_CANTHURT, (locFaction.isSafeZone() ? TL.REGION_SAFEZONE.toString() : TL.REGION_PEACEFUL.toString()));
            }
            return false;
        }

        if (locFaction.isWarZone() && Conf.warZoneFriendlyFire) {
            return true;
        }

        if (Conf.worldsIgnorePvP.contains(defenderLoc.getWorld().getName())) {
            return true;
        }

        Faction defendFaction = defender.getFaction();
        Faction attackFaction = attacker.getFaction();

        if (attackFaction.isWilderness() && Conf.disablePVPForFactionlessPlayers) {
            if (notify) {
                attacker.msg(TL.PLAYER_PVP_REQUIREFACTION);
            }
            return false;
        } else if (defendFaction.isWilderness()) {
            if (defLocFaction == attackFaction && Conf.enablePVPAgainstFactionlessInAttackersLand) {
                // Allow PVP vs. Factionless in attacker's faction territory
                return true;
            } else if (Conf.disablePVPForFactionlessPlayers) {
                if (notify) {
                    attacker.msg(TL.PLAYER_PVP_FACTIONLESS);
                }
                return false;
            }
        }

        if (defendFaction.isPeaceful()) {
            if (notify) {
                attacker.msg(TL.PLAYER_PVP_PEACEFUL);
            }
            return false;
        } else if (attackFaction.isPeaceful()) {
            if (notify) {
                attacker.msg(TL.PLAYER_PVP_PEACEFUL);
            }
            return false;
        }

        Relation relation = defendFaction.getRelationTo(attackFaction);

        // You can not hurt neutral factions
        if (Conf.disablePVPBetweenNeutralFactions && relation.isNeutral()) {
            if (notify) {
                attacker.msg(TL.PLAYER_PVP_NEUTRAL);
            }
            return false;
        }

        // Players without faction may be hurt anywhere
        if (!defender.hasFaction()) {
            return true;
        }

        // You can never hurt faction members or allies
        if (relation.isMember() || relation.isAlly()) {
            if (notify) {
                attacker.msg(TL.PLAYER_PVP_CANTHURT, defender.describeTo(attacker));
            }
            return false;
        }

        boolean ownTerritory = defender.isInOwnTerritory();

        // You can not hurt neutrals in their own territory.
        if (ownTerritory && relation.isNeutral()) {
            if (notify) {
                attacker.msg(TL.PLAYER_PVP_NEUTRALFAIL, defender.describeTo(attacker));
                defender.msg(TL.PLAYER_PVP_TRIED, attacker.describeTo(defender, true));
            }
            return false;
        }

        // Damage will be dealt. However check if the damage should be reduced.
        /*
        if (damage > 0.0 && ownTerritory && Conf.territoryShieldFactor > 0) {
            double newDamage = Math.ceil(damage * (1D - Conf.territoryShieldFactor));
            sub.setDamage(newDamage);

            // Send message
            if (notify) {
                String perc = MessageFormat.format("{0,number,#%}", (Conf.territoryShieldFactor)); // TODO does this display correctly??
                defender.msg("<i>Enemy damage reduced by <rose>%s<i>.", perc);
            }
        } */

        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getLocation() == null) {
            return;
        }

        if (Conf.safeZoneNerfedCreatureTypes.contains(event.getEntityType()) && Board.getInstance().getFactionAt(new FLocation(event.getLocation())).noMonstersInTerritory()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        // if there is a target
        Entity target = event.getTarget();
        if (target == null) {
            return;
        }

        // We are interested in blocking targeting for certain mobs:
        if (!Conf.safeZoneNerfedCreatureTypes.contains(MiscUtil.creatureTypeFromEntity(event.getEntity()))) {
            return;
        }

        // in case the target is in a safe zone.
        if (Board.getInstance().getFactionAt(new FLocation(target.getLocation())).noMonstersInTerritory()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPaintingBreak(HangingBreakEvent event) {
        if (event.getCause() == RemoveCause.EXPLOSION) {
            Location loc = event.getEntity().getLocation();
            Faction faction = Board.getInstance().getFactionAt(new FLocation(loc));
            if (faction.noExplosionsInTerritory()) {
                // faction is peaceful and has explosions set to disabled
                event.setCancelled(true);
                return;
            }

            boolean online = faction.hasPlayersOnline();

            if ((faction.isWilderness() && !Conf.worldsNoWildernessProtection.contains(loc.getWorld().getName()) && (Conf.wildernessBlockCreepers || Conf.wildernessBlockFireballs || Conf.wildernessBlockTNT)) ||
                    (faction.isNormal() && (online ? (Conf.territoryBlockCreepers || Conf.territoryBlockFireballs || Conf.territoryBlockTNT) : (Conf.territoryBlockCreepersWhenOffline || Conf.territoryBlockFireballsWhenOffline || Conf.territoryBlockTNTWhenOffline))) ||
                    (faction.isWarZone() && (Conf.warZoneBlockCreepers || Conf.warZoneBlockFireballs || Conf.warZoneBlockTNT)) ||
                    faction.isSafeZone()) {
                // explosion which needs prevention
                event.setCancelled(true);
            }
        }

        if (!(event instanceof HangingBreakByEntityEvent)) {
            return;
        }

        Entity breaker = ((HangingBreakByEntityEvent) event).getRemover();
        if (!(breaker instanceof Player)) {
            return;
        }

        if (!FactionsBlockListener.playerCanBuildDestroyBlock((Player) breaker, event.getEntity().getLocation(), "remove paintings", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPaintingPlace(HangingPlaceEvent event) {
        if (!FactionsBlockListener.playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getLocation(), "place paintings", false)) {
            event.setCancelled(true);
            // Fix: update player's inventory to avoid items glitches
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();

        // for now, only interested in Enderman and Wither boss tomfoolery
        if (!(entity instanceof Enderman) && !(entity instanceof Wither)) {
            return;
        }

        Location loc = event.getBlock().getLocation();

        if (entity instanceof Enderman) {
            if (stopEndermanBlockManipulation(loc)) {
                event.setCancelled(true);
            }
        } else if (entity instanceof Wither) {
            Faction faction = Board.getInstance().getFactionAt(new FLocation(loc));
            // it's a bit crude just using fireball protection, but I'd rather not add in a whole new set of xxxBlockWitherExplosion or whatever
            if ((faction.isWilderness() && Conf.wildernessBlockFireballs && !Conf.worldsNoWildernessProtection.contains(loc.getWorld().getName())) ||
                    (faction.isNormal() && (faction.hasPlayersOnline() ? Conf.territoryBlockFireballs : Conf.territoryBlockFireballsWhenOffline)) ||
                    (faction.isWarZone() && Conf.warZoneBlockFireballs) ||
                    faction.isSafeZone()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTravel(PlayerPortalEvent event) {
        if (!SavageFactionsPlugin.plugin.getConfig().getBoolean("portals.limit", false)) {
            return; // Don't do anything if they don't want us to.
        }

        TravelAgent agent = event.getPortalTravelAgent();

        // If they aren't able to find a portal, it'll try to create one.
        if (event.useTravelAgent() && agent.getCanCreatePortal() && agent.findPortal(event.getTo()) == null) {
            FLocation loc = new FLocation(event.getTo());
            Faction faction = Board.getInstance().getFactionAt(loc);
            if (faction.isWilderness()) {
                return; // We don't care about wilderness.
            } else if (!faction.isNormal() && !event.getPlayer().isOp()) {
                // Don't let non ops make portals in safezone or warzone.
                event.setCancelled(true);
                return;
            }

            FPlayer fp = FPlayers.getInstance().getByPlayer(event.getPlayer());
            String mininumRelation = SavageFactionsPlugin.plugin.getConfig().getString("portals.minimum-relation", "MEMBER"); // Defaults to Neutral if typed wrong.
            if (!fp.getFaction().getRelationTo(faction).isAtLeast(Relation.fromString(mininumRelation))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            if (e.getEntity() instanceof Player) {
                Player victim = (Player) e.getEntity();
                Player attacker = (Player) e.getDamager();
                FPlayer fvictim = FPlayers.getInstance().getByPlayer(victim);
                FPlayer fattacker = FPlayers.getInstance().getByPlayer(attacker);
                if (fattacker.getRelationTo(fvictim) == Relation.TRUCE) {
                    fattacker.msg(TL.PLAYER_PVP_CANTHURT, fvictim.describeTo(fattacker));
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBowHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Projectile) {
            if (e.getEntity() instanceof Player) {
                Projectile arrow = ((Projectile) e.getDamager());
                if (arrow.getShooter() instanceof Player) {
                    Player damager = (Player) ((Projectile) e.getDamager()).getShooter();
                    Player victim = (Player) e.getEntity();
                    FPlayer fdamager = FPlayers.getInstance().getByPlayer(damager);
                    FPlayer fvictim = FPlayers.getInstance().getByPlayer(victim);
                    if (fvictim.getRelationTo(fdamager) == Relation.TRUCE) {
                        fdamager.msg(TL.PLAYER_PVP_CANTHURT, fvictim.describeTo(fdamager));
                        e.setCancelled(true);
                    }
                    if (fvictim.getRelationTo(fdamager) == Relation.ENEMY) {
                        if (fvictim.isFlying()) {
                            fvictim.setFFlying(false, true);
                        }
                    }
                }
            }
        }
    }

    // For disabling interactions with item frames in another faction's territory
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // only need to check for item frames
        if (event.getRightClicked().getType() != EntityType.ITEM_FRAME) {
            return;
        }

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (!FactionsBlockListener.playerCanBuildDestroyBlock(player, entity.getLocation(), "use item frames", false)) {
            event.setCancelled(true);
        }
    }

    // For disabling interactions with armor stands in another faction's territory
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();

        // only need to check for armor stand and item frames
        if (entity.getType() != EntityType.ARMOR_STAND) {
            return;
        }

        if (!FactionsBlockListener.playerCanBuildDestroyBlock(event.getPlayer(), entity.getLocation(), "use armor stands", false)) {
            event.setCancelled(true);
        }
    }

    private boolean stopEndermanBlockManipulation(Location loc) {
        if (loc == null) {
            return false;
        }
        // quick check to see if all Enderman deny options are enabled; if so, no need to check location
        if (Conf.wildernessDenyEndermanBlocks &&
                Conf.territoryDenyEndermanBlocks &&
                Conf.territoryDenyEndermanBlocksWhenOffline &&
                Conf.safeZoneDenyEndermanBlocks &&
                Conf.warZoneDenyEndermanBlocks) {
            return true;
        }

        FLocation fLoc = new FLocation(loc);
        Faction claimFaction = Board.getInstance().getFactionAt(fLoc);

        if (claimFaction.isWilderness()) {
            return Conf.wildernessDenyEndermanBlocks;
        } else if (claimFaction.isNormal()) {
            return claimFaction.hasPlayersOnline() ? Conf.territoryDenyEndermanBlocks : Conf.territoryDenyEndermanBlocksWhenOffline;
        } else if (claimFaction.isSafeZone()) {
            return Conf.safeZoneDenyEndermanBlocks;
        } else if (claimFaction.isWarZone()) {
            return Conf.warZoneDenyEndermanBlocks;
        }

        return false;
    }

    ////////////////////////////////////////////////////
    //  FACTION SPAWNER UPGRADES
    ////////////////////////////////////////////////////

    @EventHandler
    private void onSpawnerSpawn(SpawnerSpawnEvent e) {
        FLocation fLocation = new FLocation(e.getLocation());
        Faction factionAt = Board.getInstance().getFactionAt(fLocation);

        if (factionAt.isWilderness()) {
            return;
        }

        Configuration configuration = plugin.getConfiguration();
        UpgradesConfiguration upgrades = configuration.upgrades;

        if(!upgrades.isEnabled(Upgrade.SPAWNER)) {
            return;
        }

        UpgradeConfiguration spawnerConfiguration = upgrades.spawners;
        int currentLevel = factionAt.getUpgrade(Upgrade.SPAWNER);

        if (currentLevel < 0) {
            return;
        }

        UpgradeLevel upgradeLevel = spawnerConfiguration.getLevel(currentLevel);

        if(upgradeLevel == null) {
            return;
        }

        double boost = 1.0 / upgradeLevel.boost;

        CreatureSpawner spawner = e.getSpawner();
        spawner.setDelay((int) (spawner.getDelay() * boost));
    }
}
