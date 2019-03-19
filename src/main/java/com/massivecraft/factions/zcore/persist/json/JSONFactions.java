package com.massivecraft.factions.zcore.persist.json;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.zcore.persist.MemoryFaction;
import com.massivecraft.factions.zcore.persist.MemoryFactions;
import com.massivecraft.factions.zcore.util.DiscUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JSONFactions extends MemoryFactions {
    // Info on how to persist
    private final Gson gson;
    private final File file;

    public JSONFactions() {
        this.file = new File(SavageFactionsPlugin.plugin.getDataFolder(), "factions.json");
        this.gson = SavageFactionsPlugin.plugin.gson;
    }

    public Gson getGson() {
        return gson;
    }

    // -------------------------------------------- //
    // CONSTRUCTORS
    // -------------------------------------------- //

    public File getFile() {
        return file;
    }

    public void forceSave() {
        forceSave(true);
    }

    public void forceSave(boolean sync) {
        final Map<UUID, JSONFaction> entitiesThatShouldBeSaved = new HashMap<>();
        for (Faction entity : this.factions.values()) {
            entitiesThatShouldBeSaved.put(entity.getUniqueId(), (JSONFaction) entity);
        }

        saveCore(file, entitiesThatShouldBeSaved, sync);
    }

    private boolean saveCore(File target, Map<UUID, JSONFaction> entities, boolean sync) {
        return DiscUtil.writeCatch(target, this.gson.toJson(entities), sync);
    }

    public void load() {
        Map<UUID, JSONFaction> factions = this.loadCore();

        if (factions == null) {
            return;
        }
        this.factions.putAll(factions);

        super.load();
        SavageFactionsPlugin.plugin.log("Loaded " + factions.size() + " Factions");
    }

    private Map<UUID, JSONFaction> loadCore() {
        if (!this.file.exists()) {
            return new HashMap<>();
        }

        String content = DiscUtil.readCatch(this.file);

        if (content == null) {
            return null;
        }

        return this.gson.fromJson(content, new TypeToken<Map<String, JSONFaction>>() {
        }.getType());
    }

    @Override
    public Faction generateFaction(String tag) {
        return new JSONFaction(UUID.randomUUID(), tag);
    }

    @Override
    public Faction generateFaction(UUID uniqueId, String tag) {
        return new JSONFaction(uniqueId, tag);
    }

    @Override
    public void convertFrom(MemoryFactions old) {
        this.factions.putAll(Maps.transformValues(old.factions, arg0 -> new JSONFaction((MemoryFaction) arg0)));
        forceSave();
        Factions.instance = this;
    }
}
