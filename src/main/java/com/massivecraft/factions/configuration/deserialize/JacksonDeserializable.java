package com.massivecraft.factions.configuration.deserialize;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface JacksonDeserializable {

    void deserialize(JsonNode node) throws IOException;
}
