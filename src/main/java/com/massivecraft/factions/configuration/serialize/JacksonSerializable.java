package com.massivecraft.factions.configuration.serialize;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

@FunctionalInterface
public interface JacksonSerializable {

    void serialize(JsonGenerator generator) throws IOException;
}
