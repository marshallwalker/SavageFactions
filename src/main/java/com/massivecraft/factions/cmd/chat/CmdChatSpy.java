package com.massivecraft.factions.cmd.chat;

import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class CmdChatSpy extends FCommand {

	public CmdChatSpy() {
		super();
		this.aliases.add("chatspy");

		this.optionalArgs.put("on/off", "flip");

		this.permission = Permission.CHATSPY.node;
		this.disableOnLock = false;

		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeColeader = false;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		fme.setSpyingChat(this.argAsBool(0, !fme.isSpyingChat()));

		if (fme.isSpyingChat()) {
			fme.msg(TL.COMMAND_CHATSPY_ENABLE);
			SavageFactionsPlugin.plugin.log(fme.getName() + TL.COMMAND_CHATSPY_ENABLELOG.toString());
		} else {
			fme.msg(TL.COMMAND_CHATSPY_DISABLE);
			SavageFactionsPlugin.plugin.log(fme.getName() + TL.COMMAND_CHATSPY_DISABLELOG.toString());
		}
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_CHATSPY_DESCRIPTION;
	}
}