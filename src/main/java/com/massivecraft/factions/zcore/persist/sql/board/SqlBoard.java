package com.massivecraft.factions.zcore.persist.sql.board;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.zcore.persist.MemoryBoard;
import com.massivecraft.factions.zcore.persist.sql.SqlBuilder;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class SqlBoard extends MemoryBoard {
    private static final SqlBuilder sqlBuilder = SqlBuilder.getInstance();

    public SqlBoard() {
        sqlBuilder.path("board/create_table").execute();
    }

    @Override
    public boolean load() {
        SavageFactionsPlugin.plugin.log("Loading board from database...");

        sqlBuilder.path("board/select_all").queryEach(result -> {
            UUID factionId = UUID.fromString(result.getString(1));
            String worldName = result.getString(2);
            int chunkX = result.getInt(3);
            int chunkZ = result.getInt(4);

            memoryBoardMap.put(new FLocation(worldName, chunkX, chunkZ), factionId);
        });

        SavageFactionsPlugin.plugin.log("Loaded " + memoryBoardMap.size() + " claims!");
        return true;
    }

    @Override
    public void setFactionAt(Faction faction, FLocation flocation) {
        super.setFactionAt(faction, flocation);

        sqlBuilder
                .path("board/insert")
                .args(faction.getUniqueId(),
                        flocation.getWorldName(),
                        flocation.getX(),
                        flocation.getZ())
                .executeAsyncBatched();
    }

    @Override
    public void removeAt(FLocation flocation) {
        super.removeAt(flocation);

        sqlBuilder
                .path("board/remove_by_chunk")
                .args(flocation.getWorldName(),
                        flocation.getX(),
                        flocation.getZ())
                .executeAsync();
    }

    @Override
    public void cleanByFactionId(UUID factionId) {
        super.cleanByFactionId(factionId);

        sqlBuilder
                .path("board/remove_by_faction_id")
                .args(factionId)
                .executeAsync();
    }

    @Override
    public void clean() {
        Iterator<Map.Entry<FLocation, UUID>> iterator = memoryBoardMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<FLocation, UUID> entry = iterator.next();

            if (!Factions.getInstance().isValidFactionId(entry.getValue())) {
                SavageFactionsPlugin.plugin.log("Board cleaner removed " + entry.getValue() + " from " + entry.getKey());

                sqlBuilder
                        .path("board/remove_by_faction_id")
                        .args(entry.getValue())
                        .executeAsync();
                iterator.remove();
            }
        }
    }

    @Override
    public void forceSave() {
        //do nothing
    }

    @Override
    public void forceSave(boolean sync) {
        //do nothing
    }
}
