package com.massivecraft.factions;

import com.massivecraft.factions.zcore.persist.json.JsonBoard;
import com.massivecraft.factions.zcore.persist.sql.board.SqlBoard;
import mkremins.fanciful.FancyMessage;
import org.bukkit.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public abstract class Board {
    protected static Board instance = getBoardImpl();

    private static Board getBoardImpl() {
        switch (Conf.backEnd) {
            default:
            case JSON:
                return new JsonBoard();

            case SQL:
                return new SqlBoard();
        }
    }

    public static Board getInstance() {
        return instance;
    }

    public abstract void load() throws IOException;

    public abstract void save() throws IOException;

    public abstract UUID getIdAt(FLocation flocation);

    public abstract Faction getFactionAt(FLocation flocation);

    public abstract void setIdAt(UUID id, FLocation flocation);

    public abstract void setFactionAt(Faction faction, FLocation flocation);

    public abstract void removeAt(FLocation flocation);

    public abstract Set<FLocation> getAllClaims(UUID factionId);

    public abstract Set<FLocation> getAllClaims(Faction faction);

    // not to be confused with claims, ownership referring to further member-specific ownership of a claim
    public abstract void clearOwnershipAt(FLocation flocation);

    public abstract void unclaimAll(UUID factionId);

    public abstract void unclaimAllInWorld(UUID factionId, World world);

    // Is this coord NOT completely surrounded by coords claimed by the same faction?
    // Simpler: Is there any nearby coord with a faction other than the faction here?
    public abstract boolean isBorderLocation(FLocation flocation);

    // Is this coord connected to any coord claimed by the specified faction?
    public abstract boolean isConnectedLocation(FLocation flocation, Faction faction);

    public abstract boolean hasFactionWithin(FLocation flocation, Faction faction, int radius);

    //----------------------------------------------//
    // Cleaner. Remove orphaned foreign keys
    //----------------------------------------------//

    public abstract void clean();

    public abstract void cleanByFactionId(UUID factionId);

    //----------------------------------------------//
    // Coord count
    //----------------------------------------------//

    public abstract int getFactionCoordCount(UUID factionId);

    public abstract int getFactionCoordCount(Faction faction);

    public abstract int getFactionCoordCountInWorld(Faction faction, String worldName);

    //----------------------------------------------//
    // Map generation
    //----------------------------------------------//

    /*
     * The map is relative to a coord and a faction north is in the direction of decreasing x east is in the direction
     * of decreasing z
     */
    public abstract ArrayList<FancyMessage> getMap(FPlayer fPlayer, FLocation flocation, double inDegrees);
}
