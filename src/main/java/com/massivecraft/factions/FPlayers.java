package com.massivecraft.factions;

import com.massivecraft.factions.zcore.persist.MemoryFPlayers;
import com.massivecraft.factions.zcore.persist.json.JSONFPlayers;
import com.massivecraft.factions.zcore.persist.sql.fplayer.SqlFPlayers;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public abstract class FPlayers {
    protected static FPlayers instance = getFPlayersImpl();

    public static FPlayers getInstance() {
        return instance;
    }

    private static FPlayers getFPlayersImpl() {
        switch (Conf.backEnd) {
            default:
            case JSON:
                return new JSONFPlayers();
            case SQL:
                return new SqlFPlayers();
        }
    }

    public abstract void load() throws IOException;

    public abstract void save() throws IOException;

    public abstract void convertFrom(MemoryFPlayers old);

    public abstract FPlayer generateFPlayer(UUID uniqueId);

    public abstract FPlayer getById(UUID uniqueId);

    public abstract FPlayer getByPlayer(Player player);

    public abstract FPlayer getByOfflinePlayer(OfflinePlayer player);

    public abstract void removeById(UUID uniqueId);

    public abstract void clean();

    public abstract Collection<FPlayer> getOnlinePlayers();

    public abstract Collection<FPlayer> getAllFPlayers();
}
