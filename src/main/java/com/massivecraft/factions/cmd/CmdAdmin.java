package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;

public class CmdAdmin extends FCommand {

	public CmdAdmin() {
		super();

		aliases.add("admin");
		aliases.add("setadmin");
		aliases.add("leader");
		aliases.add("setleader");

		requiredArgs.add("player name");

		permission = Permission.ADMIN.node;
	}

	@Override
	public void perform() {
		FPlayer targetFPlayer = argAsBestFPlayerMatch(0);

		if (targetFPlayer == null) {
			return;
		}

		boolean permAny = Permission.ADMIN_ANY.has(sender, false);
		Faction targetFaction = targetFPlayer.getFaction();

		if (targetFaction != myFaction && !permAny) {
			msg(TL.COMMAND_ADMIN_NOTMEMBER, targetFPlayer.describeTo(fme, true));
			return;
		}

		if (fme != null && fme.getRole() != Role.LEADER && !permAny) {
			msg(TL.COMMAND_ADMIN_NOTADMIN);
			return;
		}

		if (targetFPlayer == fme && !permAny) {
			msg(TL.COMMAND_ADMIN_TARGETSELF);
			return;
		}

		// only perform a FPlayerJoinEvent when newLeader isn't actually in the faction
		if (targetFPlayer.getFaction() != targetFaction) {
			FPlayerJoinEvent event = new FPlayerJoinEvent(fme, targetFaction, FPlayerJoinEvent.PlayerJoinReason.LEADER);
			Bukkit.getServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return;
			}
		}

		FPlayer targetFactionLeader = targetFaction.getFPlayerLeader();

		if (targetFPlayer == targetFactionLeader && targetFPlayer.getFaction().getSize() == 1) {
			msg(TL.COMMAND_ADMIN_NOMEMBERS);
			return;
		}

		// if target player is currently targetFactionLeader, demote and replace him
		if (targetFPlayer == targetFactionLeader) {
			targetFaction.promoteNewLeader();
			msg(TL.COMMAND_ADMIN_DEMOTES, targetFPlayer.describeTo(fme, true));
			targetFPlayer.msg(TL.COMMAND_ADMIN_DEMOTED, senderIsConsole ? TL.GENERIC_SERVERADMIN.toString() : fme.describeTo(targetFPlayer, true));
			return;
		}

		// promote target player, and demote existing targetFactionLeader if one exists
		if (targetFactionLeader != null) {
			targetFactionLeader.setRole(Role.COLEADER);
		}

		targetFPlayer.setRole(Role.LEADER);
		msg(TL.COMMAND_ADMIN_PROMOTES, targetFPlayer.describeTo(fme, true));

		// Inform all players
		for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
			fplayer.msg(TL.COMMAND_ADMIN_PROMOTED, senderIsConsole ? TL.GENERIC_SERVERADMIN.toString() : fme.describeTo(fplayer, true), targetFPlayer.describeTo(fplayer), targetFaction.describeTo(fplayer));
		}
	}

	public TL getUsageTranslation()  {
		return TL.COMMAND_ADMIN_DESCRIPTION;
	}
}