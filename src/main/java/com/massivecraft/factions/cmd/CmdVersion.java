package com.massivecraft.factions.cmd;

import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;


public class CmdVersion extends FCommand {

	public CmdVersion() {
		this.aliases.add("version");
		this.aliases.add("ver");

		//this.requiredArgs.add("");
		//this.optionalArgs.put("", "");

		this.permission = Permission.VERSION.node;
		this.disableOnLock = false;

		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		msg(TL.COMMAND_VERSION_NAME); // Did this so people can differentiate between SavageFactionsPlugin and FactionsUUID (( Requested Feature ))
		msg(TL.COMMAND_VERSION_VERSION, SavageFactionsPlugin.plugin.getDescription().getFullName());
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_VERSION_DESCRIPTION;
	}
}
