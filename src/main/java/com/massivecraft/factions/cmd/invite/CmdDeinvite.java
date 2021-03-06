package com.massivecraft.factions.cmd.invite;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.struct.Access;
import com.massivecraft.factions.struct.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.UUID;

public class CmdDeinvite extends FCommand {

	public CmdDeinvite() {
		super();
		this.aliases.add("deinvite");
		this.aliases.add("deinv");

		this.optionalArgs.put("player name", "name");
		//this.optionalArgs.put("", "");

		this.permission = Permission.DEINVITE.node;
		this.disableOnLock = true;


		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeModerator = true;
		senderMustBeColeader = false;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		List<String> baseCommands = p.getConfiguration().baseCommands;

		FPlayer you = this.argAsBestFPlayerMatch(0);
		if (!fme.isAdminBypassing()) {
			Access access = myFaction.getAccess(fme, PermissableAction.INVITE);
			if (access != Access.ALLOW && fme.getRole() != Role.LEADER) {
				fme.msg(TL.GENERIC_FPERM_NOPERMISSION, "manage invites");
				return;
			}
		}
		if (you == null) {
			FancyMessage msg = new FancyMessage(TL.COMMAND_DEINVITE_CANDEINVITE.toString()).color(ChatColor.GOLD);
			for (UUID id : myFaction.getInvites()) {
				FPlayer fp = FPlayers.getInstance().getById(id);
				String name = fp != null ? fp.getName() : id.toString();
				msg.then(name + " ").color(ChatColor.WHITE).tooltip(TL.COMMAND_DEINVITE_CLICKTODEINVITE.format(name)).command("/" + baseCommands.get(0) + " deinvite " + name);
			}
			sendFancyMessage(msg);
			return;
		}

		if (you.getFaction() == myFaction) {
			msg(TL.COMMAND_DEINVITE_ALREADYMEMBER, you.getName(), myFaction.getTag());
			msg(TL.COMMAND_DEINVITE_MIGHTWANT, p.cmdBase.cmdKick.getUseageTemplate(false));
			return;
		}

		myFaction.deinvite(you);

		you.msg(TL.COMMAND_DEINVITE_REVOKED, fme.describeTo(you), myFaction.describeTo(you));

		myFaction.msg(TL.COMMAND_DEINVITE_REVOKES, fme.describeTo(myFaction), you.describeTo(myFaction));
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_DEINVITE_DESCRIPTION;
	}

}
