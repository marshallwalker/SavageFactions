package com.massivecraft.factions.zcore.persist.sql.fplayer;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.zcore.persist.MemoryFPlayers;
import com.massivecraft.factions.zcore.persist.MemoryFactions;
import com.massivecraft.factions.zcore.persist.sql.SqlBuilder;

import java.util.UUID;

public class SqlFPlayers extends MemoryFPlayers {
    private SqlBuilder sqlBuilder = SqlBuilder.getInstance();

    public SqlFPlayers() {
        sqlBuilder.path("player/create_table").execute();
    }

    @Override
    public void load() {
        SavageFactionsPlugin.plugin.log("Loading players from database...");

        sqlBuilder.path("player/select_all").queryEach(result -> {
            UUID playerUniqueId = UUID.fromString(result.getString(1));
            fPlayers.put(playerUniqueId, new SqlFPlayer(result));
        });

        SavageFactionsPlugin.plugin.log("Loaded " + fPlayers.size() + " players!");
    }

    @Override
    public FPlayer generateFPlayer(UUID uniqueId) {
        SqlFPlayer player = new SqlFPlayer(uniqueId);
        fPlayers.put(player.getId(), player);

        sqlBuilder.path("player/insert")
                .args(player.getId().toString(),
                        MemoryFactions.WILDERNESS_ID.toString(),
                        player.getRole().name(),
                        player.getTitle(),
                        player.getPower(),
                        player.getPowerBoost(),
                        player.lastPowerUpdateTime,
                        player.lastLoginTime,
                        player.getChatMode().name(),
                        player.isIgnoreAllianceChat(),
                        player.getName(),
                        player.isMonitoringJoins(),
                        player.isSpyingChat(),
                        player.showScoreboard(),
                        player.warmupTask,
                        player.isAdminBypassing(),
                        player.willAutoLeave(),
                        player.getMapHeight(),
                        player.isFlying(),
                        player.isStealthEnabled(),
                        player.isInspectMode())
                .executeAsync();

        return player;
    }

    @Override
    public void removeById(UUID id) {
        super.removeById(id);

        sqlBuilder
                .path("player/remove_by", "player_id")
                .args(id)
                .executeAsync();
    }

    @Override
    public void forceSave() {
        // we don't save in bulk
    }

    @Override
    public void forceSave(boolean sync) {
        // we don't save in bulk
    }

    @Override
    public void convertFrom(MemoryFPlayers old) {
        //I don't think we care about this
    }
}
