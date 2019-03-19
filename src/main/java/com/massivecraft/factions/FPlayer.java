package com.massivecraft.factions;

import com.massivecraft.factions.participator.EconomyParticipator;
import com.massivecraft.factions.participator.RelationParticipator;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.WarmUpUtil;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
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

public interface FPlayer extends EconomyParticipator, RelationParticipator {

	/**
	 * Used to know if stealth is toggled on or off
	 *
	 * @return if stealth mode is on or not.
	 */
	boolean isStealthEnabled();

	/**
	 * Toggles the stealth of the FPlayer depending on the parameter
	 *
	 * @param stealthToggle - toggles stealth
	 */
	void setStealth(boolean stealthToggle);

	/**
	 * Sets the kills and deaths of a player.
	 */
	void login();

	/**
	 * Caches the kills and deaths of a player.
	 */
	void logout();

	/**
	 * gets the faction of a FPlayer.
	 *
	 * @return Faction of the FPlayer.
	 */
	Faction getFaction();

	/**
	 * Sets the faction of the FPlayer
	 *
	 * @param faction faction to set.
	 */
	void setFaction(Faction faction);

	/**
	 * Gets the faction ID of the player.
	 *
	 * @return FactionsID string
	 */
	UUID getFactionId();

	/**
	 * Check if a player has a faction
	 *
	 * @return boolean
	 */
	boolean hasFaction();

	/**
	 * Gets autoleave status
	 *
	 * @return boolean of the autoleave
	 */
	boolean willAutoLeave();

	void setAutoLeave(boolean autoLeave);

	long getLastFrostwalkerMessage();

	void setLastFrostwalkerMessage();

	void setMonitorJoins(boolean monitor);

	boolean isMonitoringJoins();

	Role getRole();

	void setRole(Role role);

	boolean shouldTakeFallDamage();

	void setTakeFallDamage(boolean fallDamage);

	double getPowerBoost();

	void setPowerBoost(double powerBoost);

	Faction getAutoClaimFor();

	void setAutoClaimFor(Faction faction);

	boolean isAutoSafeClaimEnabled();

	void setIsAutoSafeClaimEnabled(boolean enabled);

	boolean isAutoWarClaimEnabled();

	void setIsAutoWarClaimEnabled(boolean enabled);

	boolean isAdminBypassing();

	boolean isVanished();

	void setIsAdminBypassing(boolean val);

	ChatMode getChatMode();

	void setChatMode(ChatMode chatMode);

	boolean isIgnoreAllianceChat();

	void setIgnoreAllianceChat(boolean ignore);

	boolean isSpyingChat();

	void setSpyingChat(boolean chatSpying);

	boolean showScoreboard();

	void setShowScoreboard(boolean show);

	void resetFactionData(boolean doSpoutUpdate);

	void resetFactionData();

	long getLastLoginTime();

	void setLastLoginTime(long lastLoginTime);

	boolean isMapAutoUpdating();

	void setMapAutoUpdating(boolean mapAutoUpdating);

	boolean hasLoginPvpDisabled();

	FLocation getLastStoodAt();

	void setLastStoodAt(FLocation flocation);

	String getTitle();

	void setTitle(CommandSender sender, String title);

	String getName();

	String getTag();

	// Base concatenations:

	String getNameAndSomething(String something);

	String getNameAndTitle();

	String getNameAndTag();

	// Colored concatenations:
	// These are used in information messages

	String getNameAndTitle(Faction faction);

	String getNameAndTitle(FPlayer fplayer);

	// Chat Tag:
	// These are injected into the format of global chat messages.

	String getChatTag();

	// Colored Chat Tag
	String getChatTag(Faction faction);

	String getChatTag(FPlayer fplayer);

	int getKills();

	int getDeaths();

	//inspect Stuff

	boolean isInspectMode();

	void setInspectMode(boolean status);


	// Fly Checks

	Boolean canflyinWilderness();

	Boolean canflyinWarzone();

	Boolean canflyinSafezone();

	Boolean canflyinEnemy();

	Boolean canflyinAlly();

	Boolean canflyinTruce();

	Boolean canflyinNeutral();

	// -------------------------------
	// Relation and relation colors
	// -------------------------------

	@Override
	String describeTo(RelationParticipator that, boolean ucfirst);

	@Override
	String describeTo(RelationParticipator that);

	@Override
	Relation getRelationTo(RelationParticipator rp);

	@Override
	Relation getRelationTo(RelationParticipator rp, boolean ignorePeaceful);

	Relation getRelationToLocation();

	@Override
	ChatColor getColorTo(RelationParticipator rp);


	String getRolePrefix();

	//----------------------------------------------//
	// Health
	//----------------------------------------------//
	void heal(int amnt);


	//----------------------------------------------//
	// Power
	//----------------------------------------------//
	double getPower();

	void alterPower(double delta);

	double getPowerMax();

	double getPowerMin();

	int getPowerRounded();

	int getPowerMaxRounded();

	int getPowerMinRounded();

	void updatePower();

	void losePowerFromBeingOffline();

	void onDeath();

	//----------------------------------------------//
	// Territory
	//----------------------------------------------//
	boolean isInOwnTerritory();

	boolean isInOthersTerritory();

	boolean isInAllyTerritory();

	boolean isInNeutralTerritory();

	boolean isInEnemyTerritory();

	void sendFactionHereMessage(Faction from);

	// -------------------------------
	// Actions
	// -------------------------------

	void leave(boolean makePay);

	boolean canClaimForFaction(Faction forFaction);

	boolean canClaimForFactionAtLocation(Faction forFaction, Location location, boolean notifyFailure);

	boolean canClaimForFactionAtLocation(Faction forFaction, FLocation location, boolean notifyFailure);

	boolean attemptClaim(Faction forFaction, Location location, boolean notifyFailure);

	boolean attemptClaim(Faction forFaction, FLocation location, boolean notifyFailure);

	boolean isInVault();

	void setInVault(boolean status);

	void msg(String str, Object... args);

	UUID getId();

	void setId(UUID id);

	Player getPlayer();

	boolean isOnline();

	void sendMessage(String message);

	void sendMessage(List<String> messages);

	void sendFancyMessage(FancyMessage message);

	void sendFancyMessage(List<FancyMessage> message);

	int getMapHeight();

	void setMapHeight(int height);

	boolean isOnlineAndVisibleTo(Player me);

	void remove();

	boolean isOffline();

	boolean isFlying();

	void setFlying(boolean fly);

	void setFFlying(boolean fly, boolean damage);

	boolean canFlyAtLocation();

	boolean canFlyAtLocation(FLocation location);

	boolean checkIfNearbyEnemies();


	// -------------------------------
	// Warmups
	// -------------------------------

	boolean isWarmingUp();

	WarmUpUtil.Warmup getWarmupType();

	void addWarmup(WarmUpUtil.Warmup warmup, int taskId);

	void stopWarmup();

	void clearWarmup();

	void setChatCallback(Consumer<String> consumer);

	void setChatCallback(Consumer<String> consumer, Runnable action, int delay, TimeUnit unit);

	Consumer<String> getChatCallback();
}