package com.massivecraft.factions.zcore.persist.json;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.zcore.persist.MemoryBoard;
import com.massivecraft.factions.zcore.util.DiscUtil;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;


public class JSONBoard extends MemoryBoard {
	private static transient File file = new File(SavageFactionsPlugin.plugin.getDataFolder(), "board.json");

	// -------------------------------------------- //
	// Persistance
	// -------------------------------------------- //

	public Map<String, Map<String, UUID>> dumpAsSaveFormat() {
		Map<String, Map<String, UUID>> worldCoordIds = new HashMap<>();

		String worldName, coords;
		UUID id;

		for (Entry<FLocation, UUID> entry : memoryBoardMap.entrySet()) {
			worldName = entry.getKey().getWorldName();
			coords = entry.getKey().getCoordString();
			id = entry.getValue();
			if (!worldCoordIds.containsKey(worldName)) {
				worldCoordIds.put(worldName, new TreeMap<>());
			}

			worldCoordIds.get(worldName).put(coords, id);
		}

		return worldCoordIds;
	}

	public void loadFromSaveFormat(Map<String, Map<String, UUID>> worldCoordIds) {
		memoryBoardMap.clear();

		String worldName;
		String[] coords;
		int x, z;
		UUID factionId;

		for (Entry<String, Map<String, UUID>> entry : worldCoordIds.entrySet()) {
			worldName = entry.getKey();
			for (Entry<String, UUID> entry2 : entry.getValue().entrySet()) {
				coords = entry2.getKey().trim().split("[,\\s]+");
				x = Integer.parseInt(coords[0]);
				z = Integer.parseInt(coords[1]);
				factionId = entry2.getValue();
				memoryBoardMap.put(new FLocation(worldName, x, z), factionId);
			}
		}
	}

	public void forceSave() {
		forceSave(true);
	}

	public void forceSave(boolean sync) {
		DiscUtil.writeCatch(file, SavageFactionsPlugin.plugin.gson.toJson(dumpAsSaveFormat()), sync);
	}

	public boolean load() {
		SavageFactionsPlugin.plugin.log("Loading board from disk");

		if (!file.exists()) {
			SavageFactionsPlugin.plugin.log("No board to load from disk. Creating new file.");
			forceSave();
			return true;
		}

		try {
			Type type = new TypeToken<Map<String, Map<String, String>>>() {
			}.getType();
			Map<String, Map<String, UUID>> worldCoordIds = SavageFactionsPlugin.plugin.gson.fromJson(DiscUtil.read(file), type);
			loadFromSaveFormat(worldCoordIds);
			SavageFactionsPlugin.plugin.log("Loaded " + memoryBoardMap.size() + " board locations");
		} catch (Exception e) {
			e.printStackTrace();
			SavageFactionsPlugin.plugin.log("Failed to load the board from disk.");
			return false;
		}

		return true;
	}
}
