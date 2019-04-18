package com.massivecraft.factions.zcore.persist.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.zcore.persist.MemoryBoard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;


public class JsonBoard extends MemoryBoard {
    private static transient File file = new File(SavageFactionsPlugin.plugin.getDataFolder(), "board.json");

    private final SavageFactionsPlugin plugin;
    private final ObjectMapper objectMapper;

    public JsonBoard() {
        this.plugin = SavageFactionsPlugin.plugin;
        this.objectMapper = plugin.objectMapper;
    }

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

    public void load() throws IOException {
        plugin.log("Loading board from disk");

        if (!file.exists()) {
            plugin.getLogger().info("No board to load from disk. Creating new file.");
            save();
        }



        TypeReference t = new TypeReference<Map<String, Map<String, String>>>() {};

        Map<String, Map<String, UUID>> worldCoordIds = SavageFactionsPlugin.plugin.objectMapper.readValue(file, t);
        loadFromSaveFormat(worldCoordIds);

        plugin.getLogger().info("Loaded " + memoryBoardMap.size() + " board locations");
    }

    public void save() throws IOException {
        plugin.getLogger().info("Saving board...");
        objectMapper.writeValue(file, dumpAsSaveFormat());
    }
}
