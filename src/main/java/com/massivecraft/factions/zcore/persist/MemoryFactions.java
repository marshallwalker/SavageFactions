package com.massivecraft.factions.zcore.persist;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class MemoryFactions extends Factions {
    public static final UUID WILDERNESS_ID = UUID.fromString("fd69510f-80e4-4f5e-bd2a-a8af78178f87");
    public static final UUID SAFE_ZONE_ID = UUID.fromString("6a4d7ac3-4bae-4162-94d9-4f0b4e09575b");
    public static final UUID WAR_ZONE_ID = UUID.fromString("62b9c130-c3aa-4f54-a3e6-d34a51091e3c");

    public final Map<UUID, Faction> factions = new ConcurrentHashMap<>();

    public void load() {
        createSystemFaction(WILDERNESS_ID, TL.WILDERNESS, TL.WILDERNESS_DESCRIPTION);
        createSystemFaction(SAFE_ZONE_ID, TL.SAFEZONE, TL.SAFEZONE_DESCRIPTION);
        createSystemFaction(WAR_ZONE_ID, TL.WARZONE, TL.WARZONE_DESCRIPTION);
    }

    private void createSystemFaction(UUID uniqueId, TL tag, TL description) {
        Faction faction = factions.computeIfAbsent(uniqueId, u -> generateFaction(uniqueId, tag.toString()));

        if (!faction.getTag().equalsIgnoreCase(tag.toString())) {
            faction.setTag(tag.toString());
        }

        if (!faction.getDescription().equalsIgnoreCase(description.toString())) {
            faction.setDescription(description.toString());
        }
    }

    public Faction createFaction(String tag) {
        Faction faction = generateFaction(tag);
        factions.put(faction.getUniqueId(), faction);
        return faction;
    }

    public Faction getFactionById(UUID uniqueId) {
        return factions.get(uniqueId);
    }

    public Faction getFactionByTag(String tag) {
        String compStr = MiscUtil.getComparisonString(tag);
        for (Faction faction : factions.values()) {
            if (faction.getComparisonTag().equals(compStr)) {
                return faction;
            }
        }
        return null;
    }

    public Faction getFactionByBestTagMatch(String tag) {
        int best = 0;
        tag = tag.toLowerCase();
        int minlength = tag.length();
        Faction bestMatch = null;
        for (Faction faction : factions.values()) {
            String candidate = faction.getTag();
            candidate = ChatColor.stripColor(candidate);
            if (candidate.length() < minlength) {
                continue;
            }
            if (!candidate.toLowerCase().startsWith(tag)) {
                continue;
            }

            // The closer to zero the better
            int lendiff = candidate.length() - minlength;
            if (lendiff == 0) {
                return faction;
            }
            if (lendiff < best || best == 0) {
                best = lendiff;
                bestMatch = faction;
            }
        }

        return bestMatch;
    }

    @Override
    public void removeFactionById(UUID uniqueId) {
        factions.remove(uniqueId).remove();
    }

    public Set<String> getFactionTags() {
        return factions.values().stream().map(Faction::getTag).collect(Collectors.toSet());
    }

    @Override
    public ArrayList<Faction> getAllFactions() {
        return new ArrayList<>(factions.values());
    }

    @Override
    public Faction getWilderness() {
        return factions.get(WILDERNESS_ID);
    }

    @Override
    public Faction getSafeZone() {
        return factions.get(SAFE_ZONE_ID);
    }

    @Override
    public Faction getWarZone() {
        return factions.get(WAR_ZONE_ID);
    }
}
