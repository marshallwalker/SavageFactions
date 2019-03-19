package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class CmdModifyPower extends FCommand {

	public CmdModifyPower() {
		super();

		aliases.add("pm");
		aliases.add("mp");
		aliases.add("modifypower");
		aliases.add("modpower");

		requiredArgs.add("name");
		requiredArgs.add("power");

		this.permission = Permission.MODIFY_POWER.node; // admin only perm.
	}

	@Override
	public void perform() {
		FPlayer targetFPlayer = argAsBestFPlayerMatch(0);
		Double power = argAsDouble(1); // returns null if not a Double.

		if (targetFPlayer == null || power == null) {
			sender.sendMessage(getHelpShort());
			return;
		}

		targetFPlayer.alterPower(power);
		int newPower = targetFPlayer.getPowerRounded(); // int so we don't have super long doubles.
		msg(TL.COMMAND_MODIFYPOWER_ADDED, power, targetFPlayer.getName(), newPower);
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_MODIFYPOWER_DESCRIPTION;
	}
}
