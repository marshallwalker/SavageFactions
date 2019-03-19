package com.massivecraft.factions.integration;

import com.earth2me.essentials.Teleport;
import com.earth2me.essentials.Trade;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.SavageFactionsPlugin;
import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;

public class Essentials {
	private static IEssentials essentials;

	public static void setup() {
		Plugin essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");

		if (essentialsPlugin instanceof IEssentials) {
			SavageFactionsPlugin.plugin.log("Successfully hooked to Essentials.");
			essentials = (IEssentials) essentialsPlugin;
			return;
		}

		SavageFactionsPlugin.plugin.log("Could not hook to Essentials, not found.");
	}

	// return false if feature is disabled or Essentials isn't available
	public static boolean handleTeleport(Player player, Location loc) {
		if (!Conf.homesTeleportCommandEssentialsIntegration || essentials == null) {
			return false;
		}

		Teleport teleport = essentials.getUser(player).getTeleport();
		Trade trade = new Trade(new BigDecimal(Conf.econCostHome), essentials);
		try {
			teleport.teleport(loc, trade, TeleportCause.PLUGIN);
		} catch (Exception e) {
			player.sendMessage(ChatColor.RED.toString() + e.getMessage());
		}
		return true;
	}

	public static boolean isVanished(Player player) {
		return essentials != null && essentials.getUser(player).isVanished();
	}
}
