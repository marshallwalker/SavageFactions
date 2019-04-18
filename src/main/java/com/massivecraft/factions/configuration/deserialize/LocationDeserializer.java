package com.massivecraft.factions.configuration.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;

public class LocationDeserializer extends StdDeserializer<Location> {

    public LocationDeserializer() {
        super(Location.class);
    }

    @Override
    public Location deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        World world = Bukkit.getWorld(node.get("world").asText());

        return new Location(world,
                node.get("x").asDouble(),
                node.get("y").asDouble(),
                node.get("z").asDouble(),
                node.get("yaw").asLong(),
                node.get("pitch").asLong());
    }
}
