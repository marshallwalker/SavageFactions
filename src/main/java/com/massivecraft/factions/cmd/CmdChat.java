package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;

public class CmdChat extends FCommand {

	public CmdChat() {
		super();
		this.aliases.add("c");
		this.aliases.add("chat");

		//this.requiredArgs.add("");
		this.optionalArgs.put("mode", "next");

		this.permission = Permission.CHAT.node;
		this.disableOnLock = false;


		senderMustBePlayer = true;
		senderMustBeMember = true;
		senderMustBeModerator = false;
		senderMustBeColeader = false;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		if (!Conf.factionOnlyChat) {
			msg(TL.COMMAND_CHAT_DISABLED.toString());
			return;
		}

		String modeString = this.argAsString(0);
		ChatMode modeTarget = fme.getChatMode().getNext();

		if (modeString != null) {
			modeString = modeString.toLowerCase();
			// Only allow Mods and higher rank to switch to this channel.
			if (modeString.startsWith("m")) {
				if (!fme.getRole().isAtLeast(Role.MODERATOR)) {
					msg(TL.COMMAND_CHAT_MOD_ONLY);
					return;
				} else modeTarget = ChatMode.MOD;
			} else if (modeString.startsWith("p")) {
				modeTarget = ChatMode.PUBLIC;
			} else if (modeString.startsWith("a")) {
				modeTarget = ChatMode.ALLIANCE;
			} else if (modeString.startsWith("f")) {
				modeTarget = ChatMode.FACTION;
			} else if (modeString.startsWith("t")) {
				modeTarget = ChatMode.TRUCE;
			} else {
				msg(TL.COMMAND_CHAT_INVALIDMODE);
				return;
			}
		}

		fme.setChatMode(modeTarget);

		switch (fme.getChatMode()) {
			case MOD:
				msg(TL.COMMAND_CHAT_MODE_MOD);
				break;
			case PUBLIC:
				msg(TL.COMMAND_CHAT_MODE_PUBLIC);
				break;
			case ALLIANCE:
				msg(TL.COMMAND_CHAT_MODE_ALLIANCE);
				break;
			case TRUCE:
				msg(TL.COMMAND_CHAT_MODE_TRUCE);
				break;
			default:
				msg(TL.COMMAND_CHAT_MODE_FACTION);
				break;
		}
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_CHAT_DESCRIPTION;
	}
}