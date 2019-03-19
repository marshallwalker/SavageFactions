package com.massivecraft.factions.zcore.persist.sql.warp;

import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.zcore.persist.MemoryFWarp;
import com.massivecraft.factions.zcore.persist.sql.SqlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SqlFWarp extends MemoryFWarp {
    private static final SqlBuilder sqlBuilder = SqlBuilder.getInstance();

    private final UUID factionId;

    public SqlFWarp(UUID factionId, String name, String password, LazyLocation location) {
        super(name, password, location);
        this.factionId = factionId;
    }

    public SqlFWarp(UUID factionId, ResultSet result) throws SQLException {
        this(factionId,
                result.getString(1),
                result.getString(2),
                new LazyLocation(result.getString(3),
                        result.getDouble(4),
                        result.getDouble(5),
                        result.getDouble(6),
                        result.getFloat(7),
                        result.getFloat(8)));
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(password);

        sqlBuilder.path("faction/warp/set_password")
                .args(password,
                        name,
                        factionId)
                .executeAsync();
    }

    @Override
    public void setLocation(LazyLocation location) {
        super.setLocation(location);

        sqlBuilder.path("faction/warp/set_location")
                .args(location.getWorldName(),
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        location.getYaw(),
                        location.getPitch(),
                        name,
                        factionId)
                .executeAsync();
    }
}
