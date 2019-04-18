package com.massivecraft.factions;

import com.massivecraft.factions.zcore.persist.MemoryFactions;
import com.massivecraft.factions.zcore.persist.json.JsonFactions;
import com.massivecraft.factions.zcore.persist.sql.faction.SqlFactions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public abstract class Factions {
    protected static Factions instance = getFactionsImpl();

    public static Factions getInstance() {
        return instance;
    }

    private static Factions getFactionsImpl() {
        switch (Conf.backEnd) {
            default:
            case JSON:
                return new JsonFactions();

            case SQL:
                return new SqlFactions();
        }
    }

    public abstract void load() throws IOException;

    public abstract void save() throws IOException;

    public abstract Faction generateFaction(UUID uniqueId, String tag);

    public abstract Faction generateFaction(String tag);

    public abstract Faction createFaction(String tag);

    public abstract Faction getFactionById(UUID uniqueId);

    public abstract Faction getFactionByTag(String tag);

    public abstract Faction getFactionByBestTagMatch(String tag);

    public abstract void removeFactionById(UUID uniqueId);

    public boolean isTagTaken(String tag) {
        return getFactionByTag(tag) != null;
    }

    public boolean isValidFactionId(UUID uniqueId) {
        return getFactionById(uniqueId) != null;
    }

    public abstract Set<String> getFactionTags();

    public abstract ArrayList<Faction> getAllFactions();

    public abstract Faction getWilderness();

    public abstract Faction getSafeZone();

    public abstract Faction getWarZone();
}
