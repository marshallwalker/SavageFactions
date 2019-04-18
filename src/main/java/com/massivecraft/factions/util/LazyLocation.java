package com.massivecraft.factions.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.massivecraft.factions.configuration.deserialize.JacksonDeserializable;
import com.massivecraft.factions.configuration.serialize.JacksonSerializable;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * This class provides a lazy-load Location, so that World doesn't need to be initialized
 * yet when an object of this class is created, only when the Location is first accessed.
 */

@NoArgsConstructor
public class LazyLocation implements Serializable, JacksonSerializable, JacksonDeserializable {
    private static final long serialVersionUID = -6049901271320963314L;
    private transient Location location = null;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    public LazyLocation(Location loc) {
        setLocation(loc);
    }

    public LazyLocation(final String worldName, final double x, final double y, final double z) {
        this(worldName, x, y, z, 0, 0);
    }

    public LazyLocation(final String worldName, final double x, final double y, final double z, final float yaw, final float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public LazyLocation(ResultSet result) {
        try {
            this.worldName = result.getString(1);
            this.x = result.getDouble(2);
            this.y = result.getDouble(3);
            this.z = result.getDouble(4);
            this.yaw = result.getFloat(5);
            this.pitch = result.getFloat(6);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This returns the actual Location
    public final Location getLocation() {
        // make sure Location is initialized before returning it
        initLocation();
        return location;
    }

    // change the Location
    public final void setLocation(Location loc) {
        this.location = loc;
        this.worldName = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }


    // This initializes the Location
    private void initLocation() {
        // if location is already initialized, simply return
        if (location != null) {
            return;
        }

        // get World; hopefully it's initialized at this point
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }

        // store the Location for future calls, and pass it on
        location = new Location(world, x, y, z, yaw, pitch);
    }


    public final String getWorldName() {
        return worldName;
    }

    public final double getX() {
        return x;
    }

    public final double getY() {
        return y;
    }

    public final double getZ() {
        return z;
    }

    public final double getPitch() {
        return pitch;
    }

    public final double getYaw() {
        return yaw;
    }

    @Override
    public void serialize(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("world", worldName);
        generator.writeNumberField("x", x);
        generator.writeNumberField("y", y);
        generator.writeNumberField("z", z);
        generator.writeNumberField("yaw", yaw);
        generator.writeNumberField("pitch", pitch);
        generator.writeEndObject();
    }

    @Override
    public void deserialize(JsonNode node) throws IOException {
        this.worldName = node.get("world").asText();
        this.x = node.get("x").asDouble();
        this.y = node.get("y").asDouble();
        this.z = node.get("z").asDouble();
        this.yaw = node.get("yaw").asLong();
        this.pitch = node.get("pitch").asLong();
    }
}
