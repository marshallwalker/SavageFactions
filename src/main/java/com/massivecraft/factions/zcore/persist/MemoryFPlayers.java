package com.massivecraft.factions.zcore.persist;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.SavageFactionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class MemoryFPlayers extends FPlayers {
	public Map<UUID, FPlayer> fPlayers = new ConcurrentSkipListMap<>();

	@Override
	public FPlayer getById(UUID uniqueId) {
		FPlayer fPlayer = fPlayers.get(uniqueId);
		return fPlayer == null ? generateFPlayer(uniqueId) : fPlayer;
	}

	@Override
	public FPlayer getByPlayer(Player player) {
		return getById(player.getUniqueId());
	}

	@Override
	public FPlayer getByOfflinePlayer(OfflinePlayer player) {
		return getById(player.getUniqueId());
	}

	@Override
	public void removeById(UUID uniqueId) {
		fPlayers.remove(uniqueId);
	}

	public void clean() {
		for (FPlayer fplayer : fPlayers.values()) {
			if (!Factions.getInstance().isValidFactionId(fplayer.getFactionId())) {
				SavageFactionsPlugin.plugin.log("Reset faction data (invalid faction:" + fplayer.getFactionId() + ") for player " + fplayer.getName());
				fplayer.resetFactionData(false);
			}
		}
	}

	public Collection<FPlayer> getOnlinePlayers() {
		Set<FPlayer> entities = new HashSet<>();

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			entities.add(getById(player.getUniqueId()));
		}
		return entities;
	}

	@Override
	public List<FPlayer> getAllFPlayers() {
		return new ArrayList<>(fPlayers.values());
	}
}
