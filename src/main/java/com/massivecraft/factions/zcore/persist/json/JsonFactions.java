package com.massivecraft.factions.zcore.persist.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.zcore.persist.MemoryFactions;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class JsonFactions extends MemoryFactions {
    private final SavageFactionsPlugin plugin;
    private final ObjectMapper objectMapper;
    private final File file;

    public JsonFactions() {
        this.plugin = SavageFactionsPlugin.plugin;
        this.objectMapper = plugin.objectMapper;
        this.file = new File(plugin.getDataFolder(), "factions.json");
    }

    @Override
    public void load() throws IOException {
        plugin.getLogger().info("Loading factions from disk...");

        super.load();

        if (file.exists()) {
            for (JsonNode jsonNode : objectMapper.readTree(file)) {
                JsonFaction faction = objectMapper.convertValue(jsonNode, JsonFaction.class);
                factions.put(faction.getUniqueId(), faction);
            }
        }

        plugin.log("Loaded " + factions.size() + " Factions!");
    }

    @Override
    public void save() throws IOException {
        plugin.getLogger().info("Saving factions...");
        objectMapper.writeValue(file, factions.values());
    }

    @Override
    public Faction generateFaction(String tag) {
        return new JsonFaction(UUID.randomUUID(), tag);
    }

    @Override
    public Faction generateFaction(UUID uniqueId, String tag) {
        return new JsonFaction(uniqueId, tag);
    }
}
