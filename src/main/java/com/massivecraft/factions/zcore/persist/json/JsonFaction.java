package com.massivecraft.factions.zcore.persist.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.massivecraft.factions.configuration.deserialize.JacksonDeserializable;
import com.massivecraft.factions.configuration.serialize.JacksonSerializable;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.persist.MemoryFaction;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@NoArgsConstructor
public class JsonFaction extends MemoryFaction implements JacksonSerializable, JacksonDeserializable {

    public JsonFaction(UUID uniqueId, String tag) {
        super(uniqueId);

        this.tag = tag;
    }

    @Override
    public void serialize(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("id", uniqueId.toString());
        generator.writeStringField("tag", tag);
        generator.writeStringField("description", description);
        generator.writeStringField("paypal", paypal);
        generator.writeStringField("defaultRole", defaultRole.name());

        generator.writeBooleanField("permanent", permanent);
        generator.writeBooleanField("peacefulExplosionsEnabled", peacefulExplosionsEnabled);
        generator.writeBooleanField("open", open);
        generator.writeBooleanField("peaceful", peaceful);

        generator.writeNumberField("tnt", tnt);
        generator.writeNumberField("permanentPower", permanentPower);
        generator.writeNumberField("maxVaults", maxVaults);
        generator.writeNumberField("foundedDate", foundedDate);
        generator.writeNumberField("powerBoost", powerBoost);

        generator.writeArrayFieldStart("rules");

        for (String line : rules) {
            generator.writeString(line);
        }

        generator.writeEndArray();

        generator.writeObjectFieldStart("upgrades");

        for (Map.Entry<String, Integer> entry : upgrades.entrySet()) {
            generator.writeNumberField(entry.getKey(), entry.getValue());
        }

        generator.writeEndObject();
        generator.writeEndObject();
    }

    @Override
    public void deserialize(JsonNode node) throws IOException {
        this.uniqueId = UUID.fromString(node.get("id").asText());
        this.tag = node.get("tag").asText();
        this.description = node.get("description").asText();
        this.paypal = node.get("paypal").asText();
        this.defaultRole = Role.fromString(node.get("defaultRole").asText());
        this.permanent = node.get("permanent").asBoolean();
        this.peacefulExplosionsEnabled = node.get("peacefulExplosionsEnabled").asBoolean();
        this.open = node.get("open").asBoolean();
        this.peaceful = node.get("peaceful").asBoolean();
        this.tnt = node.get("tnt").asInt();
        this.permanentPower = node.get("permanentPower").asInt();
        this.maxVaults = node.get("maxVaults").asInt();
        this.foundedDate = node.get("foundedDate").asLong();
        this.powerBoost = node.get("powerBoost").asDouble();

        for (JsonNode rule : node.get("rules")) {
            rules.add(rule.asText());
        }
    }
}
