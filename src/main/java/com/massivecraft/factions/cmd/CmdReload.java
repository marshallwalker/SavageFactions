package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.Particles.ReflectionUtils;
import com.massivecraft.factions.zcore.util.TL;

public class CmdReload extends FCommand {

	public CmdReload() {
		super();
		this.aliases.add("reload");

		this.permission = Permission.RELOAD.node;
		this.disableOnLock = false;

		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeColeader = false;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		long timeInitStart = System.currentTimeMillis();
		Conf.load();
		Conf.save();
		SavageFactionsPlugin.plugin.reloadConfig();
		SavageFactionsPlugin.plugin.changeItemIDSInConfig();
		SavageFactionsPlugin.plugin.loadLang();
		int version = Integer.parseInt(ReflectionUtils.PackageType.getServerVersion().split("_")[1]);


		if (SavageFactionsPlugin.plugin.getConfig().getBoolean("enable-faction-flight")) {
			SavageFactionsPlugin.plugin.factionsFlight = true;
		}
		long timeReload = (System.currentTimeMillis() - timeInitStart);

		msg(TL.COMMAND_RELOAD_TIME, timeReload);
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_RELOAD_DESCRIPTION;
	}
}
