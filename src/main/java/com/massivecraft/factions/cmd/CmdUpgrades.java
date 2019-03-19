package com.massivecraft.factions.cmd;

import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.inventory.menu.UpgradeMenu;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class CmdUpgrades extends FCommand {
	public CmdUpgrades() {
		super();
		this.aliases.add("upgrades");
		this.aliases.add("upgrade");

		//this.requiredArgs.add("");
		this.optionalArgs.put("mobs/crops/exp", "");

		this.permission = Permission.UPGRADES.node;
		this.disableOnLock = true;

		senderMustBePlayer = true;
		senderMustBeMember = true;
		senderMustBeModerator = false;
		senderMustBeAdmin = false;

	}

	@Override
	public void perform() {
		if (!SavageFactionsPlugin.plugin.getConfig().getBoolean("fupgrades.Enabled")) {
			fme.sendMessage("This command is disabled!");
			return;
		}

		new UpgradeMenu(SavageFactionsPlugin.plugin).show(me);
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_UPGRADES_DESCRIPTION;
	}

}
