package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.UUID;

public class CmdSafeunclaimall extends FCommand {

	public CmdSafeunclaimall() {
		this.aliases.add("safeunclaimall");
		this.aliases.add("safedeclaimall");

		//this.requiredArgs.add("");
		this.optionalArgs.put("world", "all");

		this.permission = Permission.MANAGE_SAFE_ZONE.node;
		this.disableOnLock = true;

		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeColeader = false;
		senderMustBeAdmin = false;


	}

	@Override
	public void perform() {
		String worldName = argAsString(0);
		World world = null;

		if (worldName != null) {
			world = Bukkit.getWorld(worldName);
		}

		UUID id = Factions.getInstance().getSafeZone().getUniqueId();

		if (world == null) {
			Board.getInstance().unclaimAll(id);
		} else {
			Board.getInstance().unclaimAllInWorld(id, world);
		}

		msg(TL.COMMAND_SAFEUNCLAIMALL_UNCLAIMED);

		if (Conf.logLandUnclaims) {
			SavageFactionsPlugin.plugin.log(TL.COMMAND_SAFEUNCLAIMALL_UNCLAIMEDLOG.format(sender.getName()));
		}
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_SAFEUNCLAIMALL_DESCRIPTION;
	}

}
