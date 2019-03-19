package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradeConfiguration;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradeLevel;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradesConfiguration;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Upgrade;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;

public class CmdJoin extends FCommand {

	public CmdJoin() {
		super();
		this.aliases.add("join");

		this.requiredArgs.add("faction name");
		this.optionalArgs.put("player", "you");

		this.permission = Permission.JOIN.node;
		this.disableOnLock = true;


		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeColeader = false;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		Faction faction = this.argAsFaction(0);
		if (faction == null) {
			return;
		}

		FPlayer fplayer = this.argAsBestFPlayerMatch(1, fme, false);
		boolean samePlayer = fplayer == fme;

		if (!samePlayer && !Permission.JOIN_OTHERS.has(sender, false)) {
			msg(TL.COMMAND_JOIN_CANNOTFORCE);
			return;
		}

		if (!faction.isNormal()) {
			msg(TL.COMMAND_JOIN_SYSTEMFACTION);
			return;
		}

		if (faction == fplayer.getFaction()) {
			//TODO:TL
			msg(TL.COMMAND_JOIN_ALREADYMEMBER, fplayer.describeTo(fme, true), (samePlayer ? "are" : "is"), faction.getTag(fme));
			return;
		}

		Configuration configuration = SavageFactionsPlugin.plugin.getConfiguration();
		UpgradesConfiguration upgrades = configuration.upgrades;
		UpgradeConfiguration slots = upgrades.slots;

		int factionLimit = Conf.factionMemberLimit;

		if(upgrades.enabled && slots.enabled && factionLimit > 0) {
			int currentLevel = faction.getUpgrade(Upgrade.SLOTS);
			UpgradeLevel upgradeLevel = slots.getLevel(currentLevel);

			if(upgradeLevel != null ) {
				factionLimit += (int) upgradeLevel.boost;
			}
		}

		if (factionLimit > 0 && faction.getFPlayers().size() >= factionLimit) {
			msg(TL.COMMAND_JOIN_ATLIMIT, faction.getTag(fme), factionLimit, fplayer.describeTo(fme, false));
			return;
		}

		if (fplayer.hasFaction()) {
			//TODO:TL
			msg(TL.COMMAND_JOIN_INOTHERFACTION, fplayer.describeTo(fme, true), (samePlayer ? "your" : "their"));
			return;
		}

		if (!Conf.canLeaveWithNegativePower && fplayer.getPower() < 0) {
			msg(TL.COMMAND_JOIN_NEGATIVEPOWER, fplayer.describeTo(fme, true));
			return;
		}

		if (!(faction.getOpen() || faction.isInvited(fplayer) || fme.isAdminBypassing() || Permission.JOIN_ANY.has(sender, false))) {
			msg(TL.COMMAND_JOIN_REQUIRESINVITATION);
			if (samePlayer) {
				faction.msg(TL.COMMAND_JOIN_ATTEMPTEDJOIN, fplayer.describeTo(faction, true));
			}
			return;
		}

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
		if (samePlayer && !canAffordCommand(Conf.econCostJoin, TL.COMMAND_JOIN_TOJOIN.toString())) {
			return;
		}

		// Check for ban
		if (!fme.isAdminBypassing() && faction.isBanned(fme)) {
			fme.msg(TL.COMMAND_JOIN_BANNED, faction.getTag(fme));
			return;
		}

		// trigger the join event (cancellable)
		FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(FPlayers.getInstance().getByPlayer(me), faction, FPlayerJoinEvent.PlayerJoinReason.COMMAND);
		Bukkit.getServer().getPluginManager().callEvent(joinEvent);
		if (joinEvent.isCancelled()) {
			return;
		}

		// then make 'em pay (if applicable)
		if (samePlayer && !payForCommand(Conf.econCostJoin, TL.COMMAND_JOIN_TOJOIN, TL.COMMAND_JOIN_FORJOIN)) {
			return;
		}

		fme.msg(TL.COMMAND_JOIN_SUCCESS, fplayer.describeTo(fme, true), faction.getTag(fme));

		if (!samePlayer) {
			fplayer.msg(TL.COMMAND_JOIN_MOVED, fme.describeTo(fplayer, true), faction.getTag(fplayer));
		}
		faction.msg(TL.COMMAND_JOIN_JOINED, fplayer.describeTo(faction, true));

		fplayer.resetFactionData();
		fplayer.setFaction(faction);
		faction.deinvite(fplayer);
		fme.setRole(faction.getDefaultRole());

		if (Conf.logFactionJoin) {
			if (samePlayer) {
				SavageFactionsPlugin.plugin.log(TL.COMMAND_JOIN_JOINEDLOG.toString(), fplayer.getName(), faction.getTag());
			} else {
				SavageFactionsPlugin.plugin.log(TL.COMMAND_JOIN_MOVEDLOG.toString(), fme.getName(), fplayer.getName(), faction.getTag());
			}
		}
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_JOIN_DESCRIPTION;
	}
}
