package com.massivecraft.factions.zcore.persist.sql.fplayer;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.WarmUpUtil;
import com.massivecraft.factions.zcore.persist.MemoryFPlayer;
import com.massivecraft.factions.zcore.persist.sql.SqlBuilder;
import org.bukkit.command.CommandSender;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SqlFPlayer extends MemoryFPlayer {
    private static final SqlBuilder sqlBuilder = SqlBuilder.getInstance();

    public SqlFPlayer(ResultSet result) throws SQLException {
        id = UUID.fromString(result.getString(1));
        factionId = UUID.fromString(result.getString(2));
        role = Role.valueOf(result.getString(3));
        title = result.getString(4);
        power = result.getDouble(5);
        powerBoost = result.getDouble(6);
        lastPowerUpdateTime = result.getLong(7);
        lastLoginTime = result.getLong(8);
        chatMode = ChatMode.valueOf(result.getString(9));
        ignoreAllianceChat = result.getBoolean(10);
        name = result.getString(11);
        monitorJoins = result.getBoolean(12);
        spyingChat = result.getBoolean(13);
        showScoreboard = result.getBoolean(14);
        warmupTask = result.getInt(15);
        isAdminBypassing = result.getBoolean(16);
        willAutoLeave = result.getBoolean(17);
        mapHeight = result.getInt(18);
        isFlying = result.getBoolean(19);
        isStealthEnabled = result.getBoolean(20);
        setInspectMode(result.getBoolean(21));
    }

    public SqlFPlayer(UUID id) {
        super(id);
    }

    @Override
    public void setStealth(boolean stealth) {
        super.setStealth(stealth);

        sqlBuilder
                .path("player/set_field", "stealth")
                .args(stealth, id)
                .executeAsync();
    }

    @Override
    public void setFaction(Faction faction) {
        super.setFaction(faction);

        sqlBuilder
                .path("player/set_field", "faction_id")
                .args(faction.getUniqueId().toString(),
                        id)
                .executeAsync();
    }

    @Override
    public void setMonitorJoins(boolean monitor) {
        super.setMonitorJoins(monitor);

        sqlBuilder
                .path("player/set_field", "monitor_joins")
                .args(monitor, id)
                .executeAsync();
    }

    @Override
    public void setRole(Role role) {
        super.setRole(role);

        sqlBuilder
                .path("player/set_field", "role")
                .args(role.name(), id)
                .executeAsync();
    }

    @Override
    public void setPowerBoost(double powerBoost) {
        super.setPowerBoost(powerBoost);

        sqlBuilder
                .path("player/set_field", "power_boost")
                .args(powerBoost, id)
                .executeAsync();
    }

    @Override
    public void setAutoLeave(boolean willLeave) {
        super.setAutoLeave(willLeave);

        sqlBuilder
                .path("player/set_field", "will_auto_leave")
                .args(willLeave, id)
                .executeAsync();
    }

    @Override
    public void setIsAdminBypassing(boolean val) {
        super.setIsAdminBypassing(val);

        sqlBuilder
                .path("player/set_field", "is_admin_bypassing")
                .args(val, id)
                .executeAsync();
    }

    @Override
    public void setChatMode(ChatMode chatMode) {
        super.setChatMode(chatMode);

        sqlBuilder
                .path("player/set_field", "chat_mode")
                .args(chatMode.name(), id)
                .executeAsync();
    }

    @Override
    public void setIgnoreAllianceChat(boolean ignore) {
        super.setIgnoreAllianceChat(ignore);

        sqlBuilder
                .path("player/set_field", "ignore_alliance_chat")
                .args(ignore, id)
                .executeAsync();
    }

    @Override
    public void setSpyingChat(boolean chatSpying) {
        super.setSpyingChat(chatSpying);

        sqlBuilder
                .path("player/set_field", "spying_chat")
                .args(chatSpying, id)
                .executeAsync();
    }

    @Override
    public void resetFactionData(boolean doSpoutUpdate) {
        super.resetFactionData(doSpoutUpdate);

        sqlBuilder
                .path("player/set_field", "faction_id")
                .args("0", id)
                .executeAsync();

        sqlBuilder
                .path("player/set_field", "chat_mode")
                .args(ChatMode.PUBLIC.name(), id)
                .executeAsync();

        sqlBuilder
                .path("player/set_field", "role")
                .args(Role.NORMAL.name(), id)
                .executeAsync();

        sqlBuilder
                .path("player/set_field", "title")
                .args("", id)
                .executeAsync();
    }

    @Override
    public void setLastLoginTime(long lastLoginTime) {
        super.setLastLoginTime(lastLoginTime);

        sqlBuilder
                .path("player/set_field", "last_login_time")
                .args(lastLoginTime, id)
                .executeAsync();

        sqlBuilder
                .path("player/set_field", "last_power_update_time")
                .args(lastLoginTime, id)
                .executeAsync();
    }

    @Override
    public void setTitle(CommandSender sender, String title) {
        super.setTitle(sender, title);

        sqlBuilder
                .path("player/set_field", "title")
                .args(title, id)
                .executeAsync();
    }

    @Override
    public void setName(String name) {
        super.setName(name);

        sqlBuilder
                .path("player/set_field", "`name`")
                .args(name, id)
                .executeAsync();
    }

    @Override
    public void alterPower(double delta) {
        super.alterPower(delta);

        sqlBuilder
                .path("player/set_field", "power")
                .args(power, id)
                .executeAsync();
    }

    @Override
    public void setShowScoreboard(boolean show) {
        super.setShowScoreboard(show);

        sqlBuilder
                .path("player/set_field", "show_scoreboard")
                .args(show, id)
                .executeAsync();
    }

    @Override
    public void setFFlying(boolean fly, boolean damage) {
        super.setFFlying(fly, damage);

        sqlBuilder
                .path("player/set_field", "is_flying")
                .args(fly, id)
                .executeAsync();
    }

    @Override
    public void setMapHeight(int height) {
        super.setMapHeight(height);

        sqlBuilder
                .path("player/set_field", "map_height")
                .args(height, id)
                .executeAsync();
    }

    @Override
    public void addWarmup(WarmUpUtil.Warmup warmup, int taskId) {
        super.addWarmup(warmup, taskId);

        sqlBuilder
                .path("player/set_field", "warmup_task")
                .args(taskId, id)
                .executeAsync();
    }

    @Override
    public void setInspectMode(boolean status) {
        super.setInspectMode(status);

        sqlBuilder
                .path("player/set_field", "inspect_mode")
                .args(status, id)
                .executeAsync();
    }

    @Override
    public void remove() {
        FPlayers.getInstance().removeById(getId());
    }
}
