package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.event.FactionDisbandEvent.PlayerDisbandReason;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.UtilFly;
import com.massivecraft.factions.struct.Access;
import com.massivecraft.factions.struct.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.UUID;


public class CmdDisband extends FCommand {
	private static HashMap<UUID, UUID> disbandMap = new HashMap<>();


	public CmdDisband() {
		super();
		this.aliases.add("disband");

		//this.requiredArgs.add("");
		this.optionalArgs.put("faction tag", "yours");

		this.permission = Permission.DISBAND.node;
		this.disableOnLock = true;


		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeColeader = false;
		senderMustBeAdmin = false;

	}

	@Override
	public void perform() {
		// The faction, default to your own.. but null if console sender.
		Faction faction = this.argAsFaction(0, fme == null ? null : myFaction);
		if (faction == null) {
			return;
		}

		boolean isMyFaction = fme != null && faction == myFaction;

		if (isMyFaction) {
			if (!assertMinRole(Role.LEADER)) {
				return;
			}
		} else {
			if (!Permission.DISBAND_ANY.has(sender, true)) {
				return;
			}
		}


		if (!fme.isAdminBypassing()) {
			Access access = faction.getAccess(fme, PermissableAction.DISBAND);
			if (fme.getRole() != Role.LEADER && faction.getFPlayerLeader() != fme && access != Access.ALLOW) {
				fme.msg(TL.GENERIC_FPERM_NOPERMISSION, "disband " + faction.getTag());
				return;
			}
		}

		if (!faction.isNormal()) {
			msg(TL.COMMAND_DISBAND_IMMUTABLE.toString());
			return;
		}
		if (faction.isPermanent()) {
			msg(TL.COMMAND_DISBAND_MARKEDPERMANENT.toString());
			return;
		}

		// check for tnt before disbanding.

		if (!disbandMap.containsKey(me.getUniqueId()) && faction.getTnt() > 0) {
			msg(TL.COMMAND_DISBAND_CONFIRM.toString().replace("{tnt}", faction.getTnt() + ""));
			disbandMap.put(me.getUniqueId(), faction.getUniqueId());
			Bukkit.getScheduler().scheduleSyncDelayedTask(SavageFactionsPlugin.plugin, new Runnable() {
				@Override
				public void run() {
					disbandMap.remove(me.getUniqueId());
				}
			}, 200L);
		} else if (faction.getUniqueId().equals(disbandMap.get(me.getUniqueId())) || faction.getTnt() == 0) {
			if (SavageFactionsPlugin.plugin.getConfig().getBoolean("faction-disband-broadcast", true)) {
				for (FPlayer follower : FPlayers.getInstance().getOnlinePlayers()) {
					String amountString = senderIsConsole ? TL.GENERIC_SERVERADMIN.toString() : fme.describeTo(follower);
					UtilFly.checkFly(this.fme, Board.getInstance().getFactionAt(new FLocation(follower)));
					if (follower.getFaction() == faction) {
						follower.msg(TL.COMMAND_DISBAND_BROADCAST_YOURS, amountString);
					} else {
						follower.msg(TL.COMMAND_DISBAND_BROADCAST_NOTYOURS, amountString, faction.getTag(follower));
					}
				}
				faction.disband(me, PlayerDisbandReason.COMMAND);
			} else {
				faction.disband(me, PlayerDisbandReason.COMMAND);
				me.sendMessage(String.valueOf(TL.COMMAND_DISBAND_PLAYER));
			}
		}
	}


	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_DISBAND_DESCRIPTION;
	}
}
