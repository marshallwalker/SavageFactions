package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.struct.Access;
import com.massivecraft.factions.struct.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class CmdKick extends FCommand {

	public CmdKick() {
		super();

		aliases.add("kick");

		optionalArgs.put("player name", "player name");

		this.permission = Permission.KICK.node;

		this.senderMustBePlayer = true;
	}

	@Override
	public void perform() {
		FPlayer toKick = this.argIsSet(0) ? this.argAsBestFPlayerMatch(0) : null;
		if (toKick == null) {
			FancyMessage msg = new FancyMessage(TL.COMMAND_KICK_CANDIDATES.toString()).color(ChatColor.GOLD);
			for (FPlayer player : myFaction.getFPlayersByRole(Role.NORMAL)) {
				String s = player.getName();
				msg.then(s + " ").color(ChatColor.WHITE).tooltip(TL.COMMAND_KICK_CLICKTOKICK.toString() + s).command("/" + Conf.baseCommandAliases.get(0) + " kick " + s);
			}
			if (fme.getRole().isAtLeast(Role.COLEADER)) {
				// For both coleader and admin, add mods.
				for (FPlayer player : myFaction.getFPlayersByRole(Role.MODERATOR)) {
					String s = player.getName();
					msg.then(s + " ").color(ChatColor.GRAY).tooltip(TL.COMMAND_KICK_CLICKTOKICK.toString() + s).command("/" + Conf.baseCommandAliases.get(0) + " kick " + s);
				}
				if (fme.getRole() == Role.LEADER) {
					// Only add coleader to this for the leader.
					for (FPlayer player : myFaction.getFPlayersByRole(Role.COLEADER)) {
						String s = player.getName();
						msg.then(s + " ").color(ChatColor.RED).tooltip(TL.COMMAND_KICK_CLICKTOKICK.toString() + s).command("/" + Conf.baseCommandAliases.get(0) + " kick " + s);
					}
				}
			}

			sendFancyMessage(msg);
			return;
		}

		if (fme == toKick) {
			msg(TL.COMMAND_KICK_SELF);
			msg(TL.GENERIC_YOUMAYWANT.toString() + p.cmdBase.cmdLeave.getUseageTemplate(false));
			return;
		}

		Faction toKickFaction = toKick.getFaction();

		if (toKickFaction.isWilderness()) {
			sender.sendMessage(TL.COMMAND_KICK_NONE.toString());
			return;
		}

		// This permission check has been cleaned to be more understandable and logical
		// Unless is admin,
		// - Check for the kick permission.
		// - Make sure the player is in the faction.
		// - Make sure the kicked player has lower rank than the kicker.
		if (!fme.isAdminBypassing()) {
			Access access = myFaction.getAccess(fme, PermissableAction.KICK);
			if (access != Access.ALLOW && fme.getRole() != Role.LEADER) {
				fme.msg(TL.GENERIC_NOPERMISSION, "kick");
				return;
			}
			if (toKickFaction != myFaction) {
				msg(TL.COMMAND_KICK_NOTMEMBER, toKick.describeTo(fme, true), myFaction.describeTo(fme));
				return;
			}
			if (toKick.getRole().value >= fme.getRole().value) {
				msg(TL.COMMAND_KICK_INSUFFICIENTRANK);
				return;
			}
			if (!Conf.canLeaveWithNegativePower && toKick.getPower() < 0) {
				msg(TL.COMMAND_KICK_NEGATIVEPOWER);
				return;
			}
		}

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
		if (!canAffordCommand(Conf.econCostKick, TL.COMMAND_KICK_TOKICK.toString())) {
			return;
		}

		// trigger the leave event (cancellable) [reason:kicked]
		FPlayerLeaveEvent event = new FPlayerLeaveEvent(toKick, toKick.getFaction(), FPlayerLeaveEvent.PlayerLeaveReason.KICKED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}

		// then make 'em pay (if applicable)
		if (!payForCommand(Conf.econCostKick, TL.COMMAND_KICK_TOKICK, TL.COMMAND_KICK_FORKICK)) {
			return;
		}

		toKickFaction.msg(TL.COMMAND_KICK_FACTION, fme.describeTo(toKickFaction, true), toKick.describeTo(toKickFaction, true));

		toKick.msg(TL.COMMAND_KICK_KICKED, fme.describeTo(toKick, true), toKickFaction.describeTo(toKick));
		if (toKickFaction != myFaction) {
			fme.msg(TL.COMMAND_KICK_KICKS, toKick.describeTo(fme), toKickFaction.describeTo(fme));
		}
		if (Conf.logFactionKick) {
			SavageFactionsPlugin.plugin.log((senderIsConsole ? "A console command" : fme.getName()) + " kicked " + toKick.getName() + " from the faction: " + toKickFaction.getTag());
		}
		if (toKick.getRole() == Role.LEADER) {
			toKickFaction.promoteNewLeader();
		}
		toKickFaction.deinvite(toKick);
		toKick.resetFactionData();
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_KICK_DESCRIPTION;
	}
}