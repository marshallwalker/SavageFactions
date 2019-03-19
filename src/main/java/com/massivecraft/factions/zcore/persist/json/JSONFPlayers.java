package com.massivecraft.factions.zcore.persist.json;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.zcore.persist.MemoryFPlayer;
import com.massivecraft.factions.zcore.persist.MemoryFPlayers;
import com.massivecraft.factions.zcore.util.DiscUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JSONFPlayers extends MemoryFPlayers {
    // Info on how to persist
    private Gson gson;
    private File file;

    public JSONFPlayers() {
        file = new File(SavageFactionsPlugin.plugin.getDataFolder(), "players.json");
        gson = SavageFactionsPlugin.plugin.gson;
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public void convertFrom(MemoryFPlayers old) {
        this.fPlayers.putAll(Maps.transformValues(old.fPlayers, new Function<FPlayer, JSONFPlayer>() {
            @Override
            public JSONFPlayer apply(FPlayer arg0) {
                return new JSONFPlayer((MemoryFPlayer) arg0);
            }
        }));
        forceSave();
        FPlayers.instance = this;
    }

    public void forceSave() {
        forceSave(true);
    }

    public void forceSave(boolean sync) {
        final Map<UUID, JSONFPlayer> entitiesThatShouldBeSaved = new HashMap<>();

        for (FPlayer entity : this.fPlayers.values()) {
            if (((MemoryFPlayer) entity).shouldBeSaved()) {
                entitiesThatShouldBeSaved.put(entity.getId(), (JSONFPlayer) entity);
            }
        }

        saveCore(file, entitiesThatShouldBeSaved, sync);
    }

    private boolean saveCore(File target, Map<UUID, JSONFPlayer> data, boolean sync) {
        return DiscUtil.writeCatch(target, this.gson.toJson(data), sync);
    }

    public void load() {
        Map<UUID, JSONFPlayer> fplayers = this.loadCore();
        if (fplayers == null) {
            return;
        }
        this.fPlayers.clear();
        this.fPlayers.putAll(fplayers);
        SavageFactionsPlugin.plugin.log("Loaded " + fPlayers.size() + " players");
    }

    private Map<UUID, JSONFPlayer> loadCore() {
        if (!this.file.exists()) {
            return new HashMap<>();
        }

        String content = DiscUtil.readCatch(this.file);
        if (content == null) {
            return null;
        }

        return this.gson.fromJson(content, new TypeToken<Map<String, JSONFPlayer>>() {
        }.getType());
    }

    @Override
    public FPlayer generateFPlayer(UUID id) {
        FPlayer player = new JSONFPlayer(id);
        fPlayers.put(player.getId(), player);
        return player;
    }
}
