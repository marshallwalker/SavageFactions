package com.massivecraft.factions.zcore.persist.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.configuration.deserialize.JacksonDeserializable;
import com.massivecraft.factions.configuration.serialize.JacksonSerializable;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.persist.MemoryFPlayer;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
public class JsonFPlayer extends MemoryFPlayer implements JacksonSerializable, JacksonDeserializable {

    public JsonFPlayer(MemoryFPlayer memoryFPlayer) {
        super(memoryFPlayer);
    }

    public JsonFPlayer(UUID uniqueId) {
        super(uniqueId);
    }

    @Override
    public void remove() {
        FPlayers.getInstance().removeById(getId());
    }

    @Override
    public void serialize(JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("id", id.toString());
        generator.writeStringField("name", name);
        generator.writeStringField("faction_id", factionId.toString());
        generator.writeStringField("role", role.name());
        generator.writeStringField("title", title);
        generator.writeStringField("chat_mode", chatMode.name());

        generator.writeNumberField("power", power);
        generator.writeNumberField("power_boost", powerBoost);
        generator.writeNumberField("last_login_time", lastLoginTime);
        generator.writeNumberField("map_height", mapHeight);

        generator.writeBooleanField("ignore_alliance_chat", ignoreAllianceChat);
        generator.writeBooleanField("monitor_joins", monitorJoins);
        generator.writeBooleanField("spying_chat", spyingChat);
        generator.writeBooleanField("show_scoreboard", showScoreboard);
        generator.writeBooleanField("admin_bypassing", isAdminBypassing);
        generator.writeBooleanField("will_auto_leave", willAutoLeave);
        generator.writeBooleanField("is_flying", isFlying);
        generator.writeBooleanField("inspect_mode", inspectMode);
        generator.writeBooleanField("stealth_enabled", isStealthEnabled);

        generator.writeEndObject();
    }

    @Override
    public void deserialize(JsonNode node) throws IOException {
        this.id = UUID.fromString(node.get("id").asText());
        this.name = node.get("name").asText();
        this.factionId = UUID.fromString(node.get("faction_id").asText());
        this.role = Role.fromString(node.get("role").asText());
        this.title = node.get("title").asText();
        this.chatMode = ChatMode.valueOf(node.get("chat_mode").asText());
        this.power = node.get("power").asDouble();
        this.powerBoost = node.get("power_boost").asDouble();
        this.lastLoginTime = node.get("last_login_time").asLong();
        this.mapHeight = node.get("map_height").asInt();
        this.ignoreAllianceChat = node.get("ignore_alliance_chat").asBoolean();
        this.monitorJoins = node.get("monitor_joins").asBoolean();
        this.spyingChat = node.get("spying_chat").asBoolean();
        this.showScoreboard = node.get("show_scoreboard").asBoolean();
        this.isAdminBypassing = node.get("admin_bypassing").asBoolean();
        this.willAutoLeave = node.get("will_auto_leave").asBoolean();
        this.isFlying = node.get("is_flying").asBoolean();
        this.inspectMode = node.get("inspect_mode").asBoolean();
        this.isStealthEnabled = node.get("stealth_enabled").asBoolean();
    }
}
