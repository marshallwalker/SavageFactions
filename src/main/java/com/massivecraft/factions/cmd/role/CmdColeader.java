package com.massivecraft.factions.cmd.role;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;

public class CmdColeader extends FCommand {
	public CmdColeader() {
		super();
		this.aliases.add("co");
		this.aliases.add("setcoleader");
		this.aliases.add("coleader");
		this.aliases.add("setco");

		this.optionalArgs.put("player name", "name");
		//this.optionalArgs.put("", "");

		this.permission = Permission.COLEADER.node;
		this.disableOnLock = true;


		senderMustBePlayer = false;
		senderMustBeMember = true;
		senderMustBeModerator = false;
		senderMustBeAdmin = true;
	}

	@Override
	public void perform() {
		FPlayer you = this.argAsBestFPlayerMatch(0);
		if (you == null) {
			FancyMessage msg = new FancyMessage(TL.COMMAND_COLEADER_CANDIDATES.toString()).color(ChatColor.GOLD);
			for (FPlayer player : myFaction.getFPlayersByRole(Role.NORMAL)) {
				String s = player.getName();
				msg.then(s + " ").color(ChatColor.WHITE).tooltip(TL.COMMAND_MOD_CLICKTOPROMOTE.toString() + s).command("/" + Conf.baseCommandAliases.get(0) + " coleader " + s);
			}
			for (FPlayer player : myFaction.getFPlayersByRole(Role.MODERATOR)) {
				String s = player.getName();
				msg.then(s + " ").color(ChatColor.WHITE).tooltip(TL.COMMAND_MOD_CLICKTOPROMOTE.toString() + s).command("/" + Conf.baseCommandAliases.get(0) + " coleader " + s);
			}

			sendFancyMessage(msg);
			return;
		}

		boolean permAny = Permission.COLEADER_ANY.has(sender, false);
		Faction targetFaction = you.getFaction();

		if (targetFaction != myFaction && !permAny) {
			msg(TL.COMMAND_MOD_NOTMEMBER, you.describeTo(fme, true));
			return;
		}

		if (fme != null && fme.getRole() != Role.LEADER && !permAny) {
			msg(TL.COMMAND_COLEADER_NOTADMIN);
			return;
		}

		if (you == fme && !permAny) {
			msg(TL.COMMAND_COLEADER_SELF);
			return;
		}

		if (you.getRole() == Role.LEADER) {
			msg(TL.COMMAND_COLEADER_TARGETISADMIN);
			return;
		}

		if (you.getRole() == Role.COLEADER) {
			// Revoke
			you.setRole(Role.MODERATOR);
			targetFaction.msg(TL.COMMAND_COLEADER_REVOKED, you.describeTo(targetFaction, true));
			msg(TL.COMMAND_COLEADER_REVOKES, you.describeTo(fme, true));
		} else {
			// Give
			you.setRole(Role.COLEADER);
			targetFaction.msg(TL.COMMAND_COLEADER_PROMOTED, you.describeTo(targetFaction, true));
			msg(TL.COMMAND_COLEADER_PROMOTES, you.describeTo(fme, true));
		}
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_COLEADER_DESCRIPTION;
	}
}
