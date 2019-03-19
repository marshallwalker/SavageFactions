package com.massivecraft.factions.zcore.persist;

import com.massivecraft.factions.*;
import com.massivecraft.factions.cmd.CmdFly;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.implementation.faction.BankConfiguration;
import com.massivecraft.factions.event.*;
import com.massivecraft.factions.event.FactionDisbandEvent.PlayerDisbandReason;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.Essentials;
import com.massivecraft.factions.integration.WorldGuard;
import com.massivecraft.factions.participator.EconomyParticipator;
import com.massivecraft.factions.participator.RelationParticipator;
import com.massivecraft.factions.scoreboards.FScoreboard;
import com.massivecraft.factions.scoreboards.sidebar.FInfoSidebar;
import com.massivecraft.factions.struct.*;
import com.massivecraft.factions.util.RelationUtil;
import com.massivecraft.factions.util.WarmUpUtil;
import com.massivecraft.factions.zcore.util.TL;
import mkremins.fanciful.FancyMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


/**
 * Logged in players always have exactly one FPlayer instance. Logged out players may or may not have an FPlayer
 * instance. They will always have one if they are part of a faction. This is because only players with a faction are
 * saved to disk (in order to not waste disk space).
 * The FPlayer is linked to a minecraft player using the player name.
 * The same instance is always returned for the same player. This means you can use the == operator. No .equals method
 * necessary.
 */

public abstract class MemoryFPlayer implements FPlayer {
    protected static final DecimalFormat economyFormatter = new DecimalFormat(TL.ECON_FORMAT.toString());

    public boolean inVault = false;
    protected UUID factionId;
    protected Role role;
    protected String title;
    protected double power;
    protected double powerBoost;
    public long lastPowerUpdateTime;
    public long lastLoginTime;
    protected ChatMode chatMode;
    protected boolean ignoreAllianceChat = false;
    protected UUID id;
    protected String name;
    protected boolean monitorJoins;
    protected boolean spyingChat = false;
    protected boolean showScoreboard = true;
    protected WarmUpUtil.Warmup warmup;
    public int warmupTask;
    protected boolean isAdminBypassing = false;
    protected transient int kills, deaths;
    protected boolean willAutoLeave = true;
    protected int mapHeight = 8; // default to old value
    protected boolean isFlying = false;
    protected transient FLocation lastStoodAt = new FLocation(); // Where did this player stand the last time we checked?
    protected transient boolean mapAutoUpdating;
    protected transient Faction autoClaimFor;
    protected transient boolean autoSafeZoneEnabled;
    protected transient boolean autoWarZoneEnabled;
    protected transient boolean loginPvpDisabled;
    protected transient long lastFrostwalkerMessage;
    protected transient boolean shouldTakeFallDamage = true;
    protected boolean isStealthEnabled = false;
    boolean playerAlerts = false;
    boolean inspectMode = false;

    protected Consumer<String> chatCallback;
    protected Integer chatCallbackId = null;

    public MemoryFPlayer() {
    }

    public MemoryFPlayer(UUID id) {
        this.id = id;
        this.resetFactionData(false);
        this.power = Conf.powerPlayerStarting;
        this.lastPowerUpdateTime = System.currentTimeMillis();
        this.lastLoginTime = System.currentTimeMillis();
        this.mapAutoUpdating = false;
        this.autoClaimFor = null;
        this.autoSafeZoneEnabled = false;
        this.autoWarZoneEnabled = false;
        this.loginPvpDisabled = Conf.noPVPDamageToOthersForXSecondsAfterLogin > 0;
        this.powerBoost = 0.0;
        this.showScoreboard = SavageFactionsPlugin.plugin.getConfig().getBoolean("scoreboard.default-enabled", false);
        this.kills = 0;
        this.deaths = 0;
        this.mapHeight = Conf.mapHeight;

//		if (!Conf.newPlayerStartingFactionID.equals(MemoryFactions.WILDERNESS_ID.toString()) && Factions.getInstance().isValidFactionId(Conf.newPlayerStartingFactionID)) {
//			this.factionId = Conf.newPlayerStartingFactionID;
//		}
    }

    public MemoryFPlayer(MemoryFPlayer other) {
        this.factionId = other.factionId;
        this.id = other.id;
        this.power = other.power;
        this.lastLoginTime = other.lastLoginTime;
        this.mapAutoUpdating = other.mapAutoUpdating;
        this.autoClaimFor = other.autoClaimFor;
        this.autoSafeZoneEnabled = other.autoSafeZoneEnabled;
        this.autoWarZoneEnabled = other.autoWarZoneEnabled;
        this.loginPvpDisabled = other.loginPvpDisabled;
        this.powerBoost = other.powerBoost;
        this.role = other.role;
        this.title = other.title;
        this.chatMode = other.chatMode;
        this.spyingChat = other.spyingChat;
        this.lastStoodAt = other.lastStoodAt;
        this.isAdminBypassing = other.isAdminBypassing;
        this.showScoreboard = SavageFactionsPlugin.plugin.getConfig().getBoolean("scoreboard.default-enabled", true);
        this.kills = other.kills;
        this.deaths = other.deaths;
        this.mapHeight = Conf.mapHeight;
    }

    public boolean isStealthEnabled() {
        return this.isStealthEnabled;
    }

    public void setStealth(boolean stealth) {
        this.isStealthEnabled = stealth;
    }

    public void login() {
        this.kills = getPlayer().getStatistic(Statistic.PLAYER_KILLS);
        this.deaths = getPlayer().getStatistic(Statistic.DEATHS);
    }

    public void logout() {
        this.kills = getPlayer().getStatistic(Statistic.PLAYER_KILLS);
        this.deaths = getPlayer().getStatistic(Statistic.DEATHS);
    }

    public Faction getFaction() {
        if (this.factionId == null) {
            this.factionId = MemoryFactions.WILDERNESS_ID;
        }
        return Factions.getInstance().getFactionById(this.factionId);
    }

    public void setFaction(Faction faction) {
        Faction oldFaction = this.getFaction();
        if (oldFaction != null) {
            oldFaction.removeFPlayer(this);
        }
        faction.addFPlayer(this);
        this.factionId = faction.getUniqueId();
    }

    public UUID getFactionId() {
        return this.factionId;
    }

    public boolean hasFaction() {
        return !factionId.equals(MemoryFactions.WILDERNESS_ID);
    }

    public void setMonitorJoins(boolean monitor) {
        this.monitorJoins = monitor;
    }

    public boolean isMonitoringJoins() {
        return this.monitorJoins;
    }

    public Role getRole() {
        // Hack to fix null roles..
        if (role == null) {
            this.role = Role.NORMAL;
        }

        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public double getPowerBoost() {
        return this.powerBoost;
    }

    public void setPowerBoost(double powerBoost) {
        this.powerBoost = powerBoost;
    }

    public boolean willAutoLeave() {
        return this.willAutoLeave;
    }

    public void setAutoLeave(boolean willLeave) {
        this.willAutoLeave = willLeave;
        SavageFactionsPlugin.plugin.debug(name + " set autoLeave to " + willLeave);
    }

    public long getLastFrostwalkerMessage() {
        return this.lastFrostwalkerMessage;
    }

    public void setLastFrostwalkerMessage() {
        this.lastFrostwalkerMessage = System.currentTimeMillis();
    }

    public Faction getAutoClaimFor() {
        return autoClaimFor;
    }

    public void setAutoClaimFor(Faction faction) {
        this.autoClaimFor = faction;
        if (this.autoClaimFor != null) {
            // TODO: merge these into same autoclaim
            this.autoSafeZoneEnabled = false;
            this.autoWarZoneEnabled = false;
        }
    }

    public boolean isAutoSafeClaimEnabled() {
        return autoSafeZoneEnabled;
    }

    public void setIsAutoSafeClaimEnabled(boolean enabled) {
        this.autoSafeZoneEnabled = enabled;
        if (enabled) {
            this.autoClaimFor = null;
            this.autoWarZoneEnabled = false;
        }
    }

    public boolean isAutoWarClaimEnabled() {
        return autoWarZoneEnabled;
    }

    public void setIsAutoWarClaimEnabled(boolean enabled) {
        this.autoWarZoneEnabled = enabled;
        if (enabled) {
            this.autoClaimFor = null;
            this.autoSafeZoneEnabled = false;
        }
    }

    public boolean isAdminBypassing() {
        return this.isAdminBypassing;
    }

    public boolean isVanished() {
        return Essentials.isVanished(getPlayer());
    }

    public void setIsAdminBypassing(boolean val) {
        this.isAdminBypassing = val;
    }

    public ChatMode getChatMode() {
        if (this.factionId.equals(MemoryFactions.WILDERNESS_ID.toString()) || !Conf.factionOnlyChat) {
            this.chatMode = ChatMode.PUBLIC;
        }
        return chatMode;
    }

    public void setChatMode(ChatMode chatMode) {
        this.chatMode = chatMode;
    }

    public boolean isIgnoreAllianceChat() {
        return ignoreAllianceChat;
    }

    public void setIgnoreAllianceChat(boolean ignore) {
        this.ignoreAllianceChat = ignore;
    }

    public boolean isSpyingChat() {
        return spyingChat;
    }

    public void setSpyingChat(boolean chatSpying) {
        this.spyingChat = chatSpying;
    }

    // -------------------------------------------- //
    // Getters And Setters
    // -------------------------------------------- //

    public void resetFactionData(boolean doSpoutUpdate) {
        // cleanByFactionId up any territory ownership in old faction, if there is one
        if (factionId != null && Factions.getInstance().isValidFactionId(this.getFactionId())) {
            Faction currentFaction = this.getFaction();
            currentFaction.removeFPlayer(this);
            if (currentFaction.isNormal()) {
                currentFaction.clearClaimOwnership(this);
            }
        }

        this.factionId = MemoryFactions.WILDERNESS_ID; // The default neutral faction
        this.chatMode = ChatMode.PUBLIC;
        this.role = Role.NORMAL;
        this.title = "";
        this.autoClaimFor = null;
    }

    public void resetFactionData() {
        this.resetFactionData(true);
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        losePowerFromBeingOffline();
        this.lastLoginTime = lastLoginTime;
        this.lastPowerUpdateTime = lastLoginTime;
        if (Conf.noPVPDamageToOthersForXSecondsAfterLogin > 0) {
            this.loginPvpDisabled = true;
        }
    }

    public boolean isMapAutoUpdating() {
        return mapAutoUpdating;
    }

    public void setMapAutoUpdating(boolean mapAutoUpdating) {
        this.mapAutoUpdating = mapAutoUpdating;
    }

    //----------------------------------------------//
    // Title, Name, Faction Tag and Chat
    //----------------------------------------------//

    // Base:

    public boolean hasLoginPvpDisabled() {
        if (!loginPvpDisabled) {
            return false;
        }
        if (this.lastLoginTime + (Conf.noPVPDamageToOthersForXSecondsAfterLogin * 1000) < System.currentTimeMillis()) {
            this.loginPvpDisabled = false;
            return false;
        }
        return true;
    }

    public FLocation getLastStoodAt() {
        return this.lastStoodAt;
    }

    public void setLastStoodAt(FLocation flocation) {
        this.lastStoodAt = flocation;
    }

    public String getTitle() {
        return this.hasFaction() ? title : TL.NOFACTION_PREFIX.toString();
    }

    public void setTitle(CommandSender sender, String title) {
        // Check if the setter has it.
        if (sender.hasPermission(Permission.TITLE_COLOR.node)) {
            title = ChatColor.translateAlternateColorCodes('&', title);
        }

        this.title = title;
    }

    // Base concatenations:

    public String getName() {
        if (this.name == null) {
            // Older versions of FactionsUUID don't save the name,
            // so `name` will be null the first time it's retrieved
            // after updating
            OfflinePlayer offline = Bukkit.getOfflinePlayer(getId());
            this.name = offline.getName() != null ? offline.getName() : getId().toString();
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return this.hasFaction() ? this.getFaction().getTag() : "";
    }

    // Colored concatenations:
    // These are used in information messages

    public String getNameAndSomething(String something) {
        String ret = this.role.getPrefix();
        if (something.length() > 0) {
            ret += something + " ";
        }
        ret += this.getName();
        return ret;
    }

    public String getNameAndTitle() {
        return this.getNameAndSomething(this.getTitle());
    }

    // Chat Tag:
    // These are injected into the format of global chat messages.

    public String getNameAndTag() {
        return this.getNameAndSomething(this.getTag());
    }

    public String getNameAndTitle(Faction faction) {
        return this.getColorTo(faction) + this.getNameAndTitle();
    }

    public String getNameAndTitle(MemoryFPlayer fplayer) {
        return this.getColorTo(fplayer) + this.getNameAndTitle();
    }

    public String getChatTag() {
        return this.hasFaction() ? String.format(Conf.chatTagFormat, this.getRole().getPrefix() + this.getTag()) : TL.NOFACTION_PREFIX.toString();
    }

    // Colored Chat Tag
    public String getChatTag(Faction faction) {
        return this.hasFaction() ? this.getRelationTo(faction).getColor() + getChatTag() : "";
    }

    // -------------------------------
    // Relation and relation colors
    // -------------------------------

    public String getChatTag(MemoryFPlayer fplayer) {
        return this.hasFaction() ? this.getColorTo(fplayer) + getChatTag() : "";
    }

    public int getKills() {
        return isOnline() ? getPlayer().getStatistic(Statistic.PLAYER_KILLS) : this.kills;
    }

    public int getDeaths() {
        return isOnline() ? getPlayer().getStatistic(Statistic.DEATHS) : this.deaths;

    }

    @Override
    public String describeTo(RelationParticipator that, boolean ucfirst) {
        return RelationUtil.describeThatToMe(this, that, ucfirst);
    }

    @Override
    public String describeTo(RelationParticipator that) {
        return RelationUtil.describeThatToMe(this, that);
    }

    @Override
    public Relation getRelationTo(RelationParticipator rp) {
        return RelationUtil.getRelationTo(this, rp);
    }

    @Override
    public Relation getRelationTo(RelationParticipator rp, boolean ignorePeaceful) {
        return RelationUtil.getRelationTo(this, rp, ignorePeaceful);
    }

    public Relation getRelationToLocation() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this);
    }

    @Override
    public ChatColor getColorTo(RelationParticipator rp) {
        return RelationUtil.getColorOfThatToMe(this, rp);
    }

    //----------------------------------------------//
    // Health
    //----------------------------------------------//
    public void heal(int amnt) {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        player.setHealth(player.getHealth() + amnt);
    }

    //----------------------------------------------//
    // Power
    //----------------------------------------------//
    public double getPower() {
        this.updatePower();
        return this.power;
    }

    public void alterPower(double delta) {
        this.power += delta;
        if (this.power > this.getPowerMax()) {
            this.power = this.getPowerMax();
        } else if (this.power < this.getPowerMin()) {
            this.power = this.getPowerMin();
        }
    }

    public double getPowerMax() {
        return Conf.powerPlayerMax + this.powerBoost;
    }

    public double getPowerMin() {
        return Conf.powerPlayerMin + this.powerBoost;
    }

    public int getPowerRounded() {
        return (int) Math.round(this.getPower());
    }

    public int getPowerMaxRounded() {
        return (int) Math.round(this.getPowerMax());
    }

    public int getPowerMinRounded() {
        return (int) Math.round(this.getPowerMin());
    }

    public void updatePower() {
        if (this.isOffline()) {
            losePowerFromBeingOffline();
            if (!Conf.powerRegenOffline) {
                return;
            }
        } else if (hasFaction() && getFaction().isPowerFrozen()) {
            return; // Don't let power regen if faction power is frozen.
        }

        long now = System.currentTimeMillis();
        long millisPassed = now - this.lastPowerUpdateTime;
        this.lastPowerUpdateTime = now;

        Player thisPlayer = this.getPlayer();
        if (thisPlayer != null && thisPlayer.isDead()) {
            return;  // don't let dead players regain power until they respawn
        }

        PowerRegenEvent powerRegenEvent = new PowerRegenEvent(getFaction(), this);
        Bukkit.getServer().getPluginManager().callEvent(powerRegenEvent);

        if (!powerRegenEvent.isCancelled())
            this.alterPower(millisPassed * Conf.powerPerMinute / 60000); // millisPerMinute : 60 * 1000
    }

    public void losePowerFromBeingOffline() {
        if (Conf.powerOfflineLossPerDay > 0.0 && this.power > Conf.powerOfflineLossLimit) {
            long now = System.currentTimeMillis();
            long millisPassed = now - this.lastPowerUpdateTime;
            this.lastPowerUpdateTime = now;

            double loss = millisPassed * Conf.powerOfflineLossPerDay / (24 * 60 * 60 * 1000);
            if (this.power - loss < Conf.powerOfflineLossLimit) {
                loss = this.power;
            }
            this.alterPower(-loss);
        }
    }

    public void onDeath() {
        this.updatePower();
        this.alterPower(-Conf.powerPerDeath);
        if (hasFaction()) {
            getFaction().setLastDeath(System.currentTimeMillis());
        }
    }

    //----------------------------------------------//
    // Territory
    //----------------------------------------------//
    public boolean isInOwnTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)) == this.getFaction();
    }

    public boolean isInOthersTerritory() {
        Faction factionHere = Board.getInstance().getFactionAt(new FLocation(this));
        return factionHere != null && factionHere.isNormal() && factionHere != this.getFaction();
    }

    public boolean isInAllyTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this).isAlly();
    }

    public boolean isInNeutralTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this).isNeutral();
    }

    public boolean isInEnemyTerritory() {
        return Board.getInstance().getFactionAt(new FLocation(this)).getRelationTo(this).isEnemy();
    }

    public void sendFactionHereMessage(Faction from) {
        Faction toShow = Board.getInstance().getFactionAt(getLastStoodAt());
        boolean showChat = true;
        if (showInfoBoard(toShow)) {
            FScoreboard.get(this).setTemporarySidebar(new FInfoSidebar(toShow));
            showChat = SavageFactionsPlugin.plugin.getConfig().getBoolean("scoreboard.also-send-chat", true);
        }
        if (showChat) {
            this.sendMessage(SavageFactionsPlugin.plugin.txt.parse(TL.FACTION_LEAVE.format(from.getTag(this), toShow.getTag(this))));
        }
    }

    // -------------------------------
    // Actions
    // -------------------------------

    /**
     * Check if the scoreboard should be shown. Simple method to be used by above method.
     *
     * @param toShow Faction to be shown.
     * @return true if should show, otherwise false.
     */
    public boolean showInfoBoard(Faction toShow) {
        return showScoreboard && !toShow.isWarZone() && !toShow.isWilderness() && !toShow.isSafeZone() && SavageFactionsPlugin.plugin.getConfig().contains("scoreboard.finfo") && SavageFactionsPlugin.plugin.getConfig().getBoolean("scoreboard.finfo-enabled", false) && FScoreboard.get(this) != null;
    }

    @Override
    public boolean showScoreboard() {
        return this.showScoreboard;
    }

    @Override
    public void setShowScoreboard(boolean show) {
        this.showScoreboard = show;
    }

    public void leave(boolean makePay) {
        Faction myFaction = this.getFaction();
        makePay = makePay && Econ.shouldBeUsed() && !this.isAdminBypassing();

        if (myFaction == null) {
            resetFactionData();
            return;
        }

        boolean perm = myFaction.isPermanent();

        if (!perm && this.getRole() == Role.LEADER && myFaction.getFPlayers().size() > 1) {
            msg(TL.LEAVE_PASSADMIN);
            return;
        }

        if (!Conf.canLeaveWithNegativePower && this.getPower() < 0) {
            msg(TL.LEAVE_NEGATIVEPOWER);
            return;
        }

        // if economy is enabled and they're not on the bypass list, make sure they can pay
        if (makePay && !Econ.hasAtLeast(this, Conf.econCostLeave, TL.LEAVE_TOLEAVE.toString())) {
            return;
        }

        FPlayerLeaveEvent leaveEvent = new FPlayerLeaveEvent(this, myFaction, FPlayerLeaveEvent.PlayerLeaveReason.LEAVE);
        Bukkit.getServer().getPluginManager().callEvent(leaveEvent);
        if (leaveEvent.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (makePay && !Econ.modifyMoney(this, -Conf.econCostLeave, TL.LEAVE_TOLEAVE.toString(), TL.LEAVE_FORLEAVE.toString())) {
            return;
        }

        // Am I the last one in the faction?
        if (myFaction.getFPlayers().size() == 1) {
            // Transfer all money
            if (Econ.shouldBeUsed()) {
                Econ.transferMoney(this, myFaction, this, myFaction.getBalance());
            }
        }

        if (myFaction.isNormal()) {
            for (FPlayer fplayer : myFaction.getFPlayersWhereOnline(true)) {
                fplayer.msg(TL.LEAVE_LEFT, this.describeTo(fplayer, true), myFaction.describeTo(fplayer));
            }

            if (Conf.logFactionLeave) {
                SavageFactionsPlugin.plugin.log(TL.LEAVE_LEFT.format(this.getName(), myFaction.getTag()));
            }
        }

        myFaction.removeAnnouncements(this);
        this.resetFactionData();

        if (myFaction.isNormal() && !perm && myFaction.getFPlayers().isEmpty()) {
            // Remove this faction
            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                fplayer.msg(TL.LEAVE_DISBANDED, myFaction.describeTo(fplayer, true));
            }

            FactionDisbandEvent disbandEvent = new FactionDisbandEvent(null, getFactionId(), PlayerDisbandReason.LEAVE);
            Bukkit.getPluginManager().callEvent(disbandEvent);

            Factions.getInstance().removeFactionById(myFaction.getUniqueId());
            if (Conf.logFactionDisband) {
                SavageFactionsPlugin.plugin.log(TL.LEAVE_DISBANDEDLOG.format(myFaction.getTag(), myFaction.getUniqueId(), this.getName()));
            }
        }
    }

    public boolean canClaimForFaction(Faction forFaction) {
        return this.isAdminBypassing() || !forFaction.isWilderness() && (forFaction == this.getFaction() && this.getRole().isAtLeast(Role.MODERATOR)) || (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(getPlayer())) || (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(getPlayer()));
    }

    public boolean canClaimForFactionAtLocation(Faction forFaction, Location location, boolean notifyFailure) {
        return canClaimForFactionAtLocation(forFaction, new FLocation(location), notifyFailure);
    }

    public boolean canClaimForFactionAtLocation(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        String error = null;
        Faction myFaction = getFaction();
        Faction currentFaction = Board.getInstance().getFactionAt(flocation);
        int ownedLand = forFaction.getLandRounded();
        int factionBuffer = SavageFactionsPlugin.plugin.getConfig().getInt("hcf.buffer-zone", 0);
        int worldBuffer = SavageFactionsPlugin.plugin.getConfig().getInt("world-border.buffer", 0);

        if (Conf.worldGuardChecking && WorldGuard.checkForRegionsInChunk(flocation)) {
            // Checks for WorldGuard regions in the chunk attempting to be claimed
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_PROTECTED.toString());
        } else if (flocation.isOutsideWorldBorder(SavageFactionsPlugin.plugin.getConfig().getInt("world-border.buffer", 0))) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_OUTSIDEWORLDBORDER.toString());
        } else if (Conf.worldsNoClaiming.contains(flocation.getWorldName())) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_DISABLED.toString());
        } else if (this.isAdminBypassing()) {
            return true;
        } else if (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(getPlayer())) {
            return true;
        } else if (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(getPlayer())) {
            return true;
        } else if (currentFaction.getAccess(this, PermissableAction.TERRITORY) == Access.ALLOW) {
            return true;
        } else if (myFaction != forFaction) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_CANTCLAIM.toString(), forFaction.describeTo(this));
        } else if (forFaction == currentFaction) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_ALREADYOWN.toString(), forFaction.describeTo(this, true));
        } else if (this.getRole().value < Role.MODERATOR.value) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_MUSTBE.toString(), Role.MODERATOR.getTranslation());
        } else if (forFaction.getFPlayers().size() < Conf.claimsRequireMinFactionMembers) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_MEMBERS.toString(), Conf.claimsRequireMinFactionMembers);
        } else if (currentFaction.isSafeZone()) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_SAFEZONE.toString());
        } else if (currentFaction.isWarZone()) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_WARZONE.toString());
        } else if (SavageFactionsPlugin.plugin.getConfig().getBoolean("hcf.allow-overclaim", true) && ownedLand >= forFaction.getPowerRounded()) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_POWER.toString());
        } else if (Conf.claimedLandsMax != 0 && ownedLand >= Conf.claimedLandsMax && forFaction.isNormal()) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_LIMIT.toString());
        } else if (currentFaction.getRelationTo(forFaction) == Relation.ALLY) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_ALLY.toString());
        } else if (Conf.claimsMustBeConnected && !this.isAdminBypassing() && myFaction.getLandRoundedInWorld(flocation.getWorldName()) > 0 && !Board.getInstance().isConnectedLocation(flocation, myFaction) && (!Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction || !currentFaction.isNormal())) {
            if (Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction) {
                error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_CONTIGIOUS.toString());
            } else {
                error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_FACTIONCONTIGUOUS.toString());
            }
        } else if (factionBuffer > 0 && Board.getInstance().hasFactionWithin(flocation, myFaction, factionBuffer)) {
            error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_TOOCLOSETOOTHERFACTION.format(factionBuffer));
        } else if (flocation.isOutsideWorldBorder(worldBuffer)) {
            if (worldBuffer > 0) {
                error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_OUTSIDEBORDERBUFFER.format(worldBuffer));
            } else {
                error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_OUTSIDEWORLDBORDER.toString());
            }
        } else if (currentFaction.isNormal()) {
            if (myFaction.isPeaceful()) {
                error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_PEACEFUL.toString(), currentFaction.getTag(this));
            } else if (currentFaction.isPeaceful()) {
                error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_PEACEFULTARGET.toString(), currentFaction.getTag(this));
            } else if (!currentFaction.hasLandInflation()) {
                // TODO more messages WARN current faction most importantly
                error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_THISISSPARTA.toString(), currentFaction.getTag(this));
            } else if (currentFaction.hasLandInflation() && !SavageFactionsPlugin.plugin.getConfig().getBoolean("hcf.allow-overclaim", true)) {
                // deny over claim when it normally would be allowed.
                error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_OVERCLAIM_DISABLED.toString());
            } else if (!Board.getInstance().isBorderLocation(flocation)) {
                error = SavageFactionsPlugin.plugin.txt.parse(TL.CLAIM_BORDER.toString());
            }
        }
        // TODO: Add more else if statements.

        if (notifyFailure && error != null) {
            msg(error);
        }
        return error == null;
    }

    public boolean attemptClaim(Faction forFaction, Location location, boolean notifyFailure) {
        return attemptClaim(forFaction, new FLocation(location), notifyFailure);
    }

    public boolean shouldBeSaved() {
        return this.hasFaction() || (this.getPowerRounded() != this.getPowerMaxRounded() && this.getPowerRounded() != (int) Math.round(Conf.powerPlayerStarting));
    }

    public void msg(String str, Object... args) {
        this.sendMessage(SavageFactionsPlugin.plugin.txt.parse(str, args));
    }

    public void msg(TL translation, Object... args) {
        this.msg(translation.toString(), args);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(getId());
    }

    public boolean isOnline() {
        return this.getPlayer() != null;
    }

    // make sure target player should be able to detect that this player is online
    public boolean isOnlineAndVisibleTo(Player player) {
        Player target = this.getPlayer();
        return target != null && player.canSee(target);
    }

    public boolean isOffline() {
        return !isOnline();
    }

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean fly) {
        setFFlying(fly, false);
    }

    public void setFFlying(boolean fly, boolean damage) {
        Player player = getPlayer();
        if (player != null) {
            player.setAllowFlight(fly);
            player.setFlying(fly);
        }

        if (!damage) {
            msg(TL.COMMAND_FLY_CHANGE, fly ? "enabled" : "disabled");
            if (!fly) {
                sendMessage(TL.COMMAND_FLY_COOLDOWN.toString().replace("{amount}", SavageFactionsPlugin.plugin.getConfig().getInt("fly-falldamage-cooldown", 3) + ""));
            }

        } else {
            msg(TL.COMMAND_FLY_DAMAGE);
        }

        // If leaving fly mode, don't let them take fall damage for x seconds.
        if (!fly) {
            int cooldown = SavageFactionsPlugin.plugin.getConfig().getInt("fly-falldamage-cooldown", 3);
            CmdFly.flyMap.remove(player.getName());

            // If the value is 0 or lower, make them take fall damage.
            // Otherwise, start a timer and have this cancel after a few seconds.
            // Short task so we're just doing it in method. Not cleanByFactionId but eh.
            if (cooldown > 0) {
                setTakeFallDamage(false);
                Bukkit.getScheduler().runTaskLater(SavageFactionsPlugin.plugin, new Runnable() {
                    @Override
                    public void run() {
                        setTakeFallDamage(true);
                    }
                }, 20L * cooldown);
            }
        }

        isFlying = fly;
    }

    public boolean isInVault() {
        return inVault;
    }

    public void setInVault(boolean status) {
        inVault = status;
    }

    public boolean canFlyAtLocation() {
        return canFlyAtLocation(lastStoodAt);
    }

    public boolean canFlyAtLocation(FLocation location) {
        Faction faction = Board.getInstance().getFactionAt(location);
        if ((faction == getFaction() && getRole() == Role.LEADER) || isAdminBypassing) {
            return true;
        }

        Access access = faction.getAccess(this, PermissableAction.FLY);
        return access == null || access == Access.UNDEFINED || access == Access.ALLOW;
    }

    public boolean shouldTakeFallDamage() {
        return this.shouldTakeFallDamage;
    }

    public void setTakeFallDamage(boolean fallDamage) {
        this.shouldTakeFallDamage = fallDamage;
    }

    // -------------------------------------------- //
    // Message Sending Helpers
    // -------------------------------------------- //

    public void sendMessage(String msg) {
        if (msg.contains("{null}")) {
            return; // user wants this message to not send
        }
        if (msg.contains("/n/")) {
            for (String s : msg.split("/n/")) {
                sendMessage(s);
            }
            return;
        }
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        player.sendMessage(msg);
    }

    public void sendMessage(List<String> msgs) {
        for (String msg : msgs) {
            this.sendMessage(msg);
        }
    }

    public void sendFancyMessage(FancyMessage message) {
        Player player = getPlayer();
        if (player == null || !player.isOnGround()) {
            return;
        }

        message.send(player);
    }

    public void sendFancyMessage(List<FancyMessage> messages) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        for (FancyMessage msg : messages) {
            msg.send(player);
        }
    }

    public int getMapHeight() {
        if (this.mapHeight < 1) {
            this.mapHeight = Conf.mapHeight;
        }

        return this.mapHeight;
    }

    public void setMapHeight(int height) {
        this.mapHeight = height > (Conf.mapHeight * 2) ? (Conf.mapHeight * 2) : height;
    }

    public String getNameAndTitle(FPlayer fplayer) {
        return this.getColorTo(fplayer) + this.getNameAndTitle();
    }

    @Override
    public String getChatTag(FPlayer fplayer) {
        return this.hasFaction() ? this.getRelationTo(fplayer).getColor() + getChatTag() : "";
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public abstract void remove();

    @Override
    public void clearWarmup() {
        if (warmup != null) {
            Bukkit.getScheduler().cancelTask(warmupTask);
            this.stopWarmup();
        }
    }

    @Override
    public void stopWarmup() {
        warmup = null;
    }

    @Override
    public boolean isWarmingUp() {
        return warmup != null;
    }

    @Override
    public WarmUpUtil.Warmup getWarmupType() {
        return warmup;
    }

    @Override
    public void addWarmup(WarmUpUtil.Warmup warmup, int taskId) {
        if (this.warmup != null) {
            this.clearWarmup();
        }
        this.warmup = warmup;
        this.warmupTask = taskId;
    }

    @Override
    public boolean checkIfNearbyEnemies() {
        Player me = this.getPlayer();
        int radius = Conf.stealthFlyCheckRadius;
        for (Entity e : me.getNearbyEntities(radius, 255, radius)) {
            if (e == null) {
                continue;
            }
            if (e instanceof Player) {
                Player eplayer = (((Player) e).getPlayer());
                if (eplayer == null) {
                    continue;
                }
                FPlayer efplayer = FPlayers.getInstance().getByPlayer(eplayer);
                if (efplayer == null) {
                    continue;
                }
                if (efplayer != null && this.getRelationTo(efplayer).equals(Relation.ENEMY) && !efplayer.isStealthEnabled()) {
                    setFlying(false);
                    msg(TL.COMMAND_FLY_ENEMY_NEAR);
                    Bukkit.getServer().getPluginManager().callEvent(new FPlayerStoppedFlying(this));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean canflyinWilderness() {
        return getPlayer().hasPermission("factions.fly.wilderness");
    }

    @Override
    public Boolean canflyinWarzone() {
        return getPlayer().hasPermission("factions.fly.warzone");

    }

    @Override
    public Boolean canflyinSafezone() {
        return getPlayer().hasPermission("factions.fly.safezone");

    }

    @Override
    public Boolean canflyinEnemy() {
        return getPlayer().hasPermission("factions.fly.enemy");

    }

    @Override
    public Boolean canflyinAlly() {
        return getPlayer().hasPermission("factions.fly.ally");

    }

    @Override
    public Boolean canflyinTruce() {
        return getPlayer().hasPermission("factions.fly.truce");

    }

    @Override
    public Boolean canflyinNeutral() {
        return getPlayer().hasPermission("factions.fly.neutral");

    }

    @Override
    public boolean isInspectMode() {
        return inspectMode;
    }

    @Override
    public void setInspectMode(boolean status) {
        inspectMode = status;
    }

    public boolean attemptClaim(Faction forFaction, FLocation flocation, boolean notifyFailure) {
        // notifyFailure is false if called by auto-claim; no need to notify on every failure for it
        // return value is false on failure, true on success

        Faction currentFaction = Board.getInstance().getFactionAt(flocation);
        int ownedLand = forFaction.getLandRounded();

        if (!this.canClaimForFactionAtLocation(forFaction, flocation, notifyFailure)) {
            return false;
        }

        // if economy is enabled and they're not on the bypass list, make sure they can pay
        boolean mustPay = Econ.shouldBeUsed() && !this.isAdminBypassing() && !forFaction.isSafeZone() && !forFaction.isWarZone();
        double cost = 0.0;
        EconomyParticipator payee = null;
        if (mustPay) {
            cost = Econ.calculateClaimCost(ownedLand, currentFaction.isNormal());


            if (Conf.econClaimUnconnectedFee != 0.0 && forFaction.getLandRoundedInWorld(flocation.getWorldName()) > 0 && !Board.getInstance().isConnectedLocation(flocation, forFaction)) {
                cost += Conf.econClaimUnconnectedFee;
            }

            Configuration configuration = SavageFactionsPlugin.plugin.getConfiguration();
            BankConfiguration bankConfiguration = configuration.faction.bank;

            if (bankConfiguration.enabled && bankConfiguration.factionPaysLandCost && hasFaction()) {
                payee = this.getFaction();
            } else {
                payee = this;
            }

            if (!Econ.hasAtLeast(payee, cost, TL.CLAIM_TOCLAIM.toString())) {
                return false;
            }
        }

        LandClaimEvent claimEvent = new LandClaimEvent(flocation, forFaction, this);
        Bukkit.getPluginManager().callEvent(claimEvent);
        if (claimEvent.isCancelled()) {
            return false;
        }

        // then make 'em pay (if applicable)
        if (mustPay && !Econ.modifyMoney(payee, -cost, TL.CLAIM_TOCLAIM.toString(), TL.CLAIM_FORCLAIM.toString())) {
            return false;
        }

        // Was an over claim
        if (currentFaction.isNormal() && currentFaction.hasLandInflation()) {
            // Give them money for over claiming.
            Econ.modifyMoney(payee, Conf.econOverclaimRewardMultiplier, TL.CLAIM_TOOVERCLAIM.toString(), TL.CLAIM_FOROVERCLAIM.toString());
        }

        // announce success
        Set<FPlayer> informTheseFPlayers = new HashSet<>();
        informTheseFPlayers.add(this);
        informTheseFPlayers.addAll(forFaction.getFPlayersWhereOnline(true));
        for (FPlayer fp : informTheseFPlayers) {
            fp.msg(TL.CLAIM_CLAIMED, this.describeTo(fp, true), forFaction.describeTo(fp), currentFaction.describeTo(fp));
        }

        Board.getInstance().setFactionAt(forFaction, flocation);

        if (Conf.logLandClaims) {
            SavageFactionsPlugin.plugin.log(TL.CLAIM_CLAIMEDLOG.toString(), this.getName(), flocation.getCoordString(), forFaction.getTag());
        }

        return true;
    }


    @Override
    public String getRolePrefix() {

        switch (getRole()) {
            case RECRUIT:
                return Conf.prefixRecruit;
            case NORMAL:
                return Conf.prefixNormal;
            case MODERATOR:
                return Conf.prefixMod;
            case COLEADER:
                return Conf.prefixCoLeader;
            case LEADER:
                return Conf.prefixLeader;
        }

        return null;
    }

    @Override
    public void setChatCallback(Consumer<String> consumer) {
        if (chatCallbackId != null) {
            SavageFactionsPlugin plugin = SavageFactionsPlugin.plugin;
            BukkitScheduler scheduler = plugin.getServer().getScheduler();
            scheduler.cancelTask(chatCallbackId);
        }

        this.chatCallback = consumer;
    }

    @Override
    public void setChatCallback(Consumer<String> consumer, Runnable action, int delay, TimeUnit unit) {
        setChatCallback(consumer);

        SavageFactionsPlugin plugin = SavageFactionsPlugin.plugin;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        long ticks = TimeUnit.SECONDS.convert(delay, unit) * 20;
        this.chatCallbackId = scheduler.runTaskLater(plugin, () -> {
            this.chatCallback = null;
            action.run();
        }, ticks).getTaskId();
    }

    @Override
    public Consumer<String> getChatCallback() {
        return chatCallback;
    }

    ////////////////////////////////////////////////////////
    //
    //   ACCOUNT
    //
    ////////////////////////////////////////////////////////

    public UUID getAccountId() {
        return this.getId();
    }

    @Override
    public double getBalance() {
        Economy economy = SavageFactionsPlugin.plugin.getEcon();
        return economy.getBalance(getPlayer());
    }

    @Override
    public String getBalanceFormatted() {
        return economyFormatter.format(getBalance());
    }

    @Override
    public boolean deposit(double amount) {
        Economy economy = SavageFactionsPlugin.plugin.getEcon();
        return economy.depositPlayer(getPlayer(), amount).transactionSuccess();
    }

    @Override
    public boolean withdraw(double amount) {
        Economy economy = SavageFactionsPlugin.plugin.getEcon();
        return economy.withdrawPlayer(getPlayer(), amount).transactionSuccess();
    }

    @Override
    public void transfer(double amount, EconomyParticipator participator) {
        Economy economy = SavageFactionsPlugin.plugin.getEcon();
        economy.withdrawPlayer(getPlayer(), amount);
        economy.depositPlayer(Bukkit.getOfflinePlayer(participator.getAccountId().toString()), amount);
    }

    @Override
    public boolean canAfford(double amount) {
        return getBalance() >= amount;
    }
}