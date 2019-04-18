package com.massivecraft.factions.zcore.persist.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.zcore.persist.MemoryFPlayers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JSONFPlayers extends MemoryFPlayers {
    private final SavageFactionsPlugin plugin;
    private final ObjectMapper objectMapper;
    private final File file;

    public JSONFPlayers() {
        this.plugin = SavageFactionsPlugin.plugin;
        this.objectMapper = plugin.objectMapper;
        this.file = new File(plugin.getDataFolder(), "players.json");
    }

    public void load() throws IOException {
        if (!file.exists()) {
            return;
        }

        plugin.getLogger().info("Loading players from disk...");
        TypeReference type = new TypeReference<List<JsonFPlayer>>() {};

        for(JsonNode node : objectMapper.readTree(file)) {
            JsonFPlayer fplayer = objectMapper.convertValue(node, JsonFPlayer.class);
            fPlayers.put(fplayer.getId(), fplayer);
        }

        plugin.log("Loaded " + fPlayers.size() + " players!");
    }

    @Override
    public void save() throws IOException {
        plugin.getLogger().info("Saving players...");
        objectMapper.writeValue(file, fPlayers.values());
    }

    public void convertFrom(MemoryFPlayers old) {
        //todo implement
    }

    @Override
    public FPlayer generateFPlayer(UUID id) {
        FPlayer player = new JsonFPlayer(id);
        fPlayers.put(player.getId(), player);
        return player;
    }
}
