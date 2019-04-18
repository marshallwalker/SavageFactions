package com.massivecraft.factions.cmd.invite;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.UUID;

public class CmdShowInvites extends FCommand {

	public CmdShowInvites() {
		super();
		aliases.add("showinvites");
		permission = Permission.SHOW_INVITES.node;

		senderMustBePlayer = true;
		senderMustBeMember = true;

	}

	@Override
	public void perform() {
		List<String> baseCommands = p.getConfiguration().baseCommands;
		FancyMessage msg = new FancyMessage(TL.COMMAND_SHOWINVITES_PENDING.toString()).color(ChatColor.GOLD);
		for (UUID id : myFaction.getInvites()) {
			FPlayer fp = FPlayers.getInstance().getById(id);
			String name = fp != null ? fp.getName() : id.toString();
			msg.then(name + " ").color(ChatColor.WHITE).tooltip(TL.COMMAND_SHOWINVITES_CLICKTOREVOKE.format(name)).command("/" + baseCommands.get(0) + " deinvite " + name);
		}

		sendFancyMessage(msg);
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_SHOWINVITES_DESCRIPTION;
	}


}
