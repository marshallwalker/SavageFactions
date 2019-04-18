package com.massivecraft.factions.configuration.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class JacksonDeserializableDeserializer<T extends JacksonDeserializable> extends StdDeserializer<T> {
    private final Class<T> typeClass;

    public JacksonDeserializableDeserializer(Class<T> typeClass) {
        super(JacksonDeserializable.class);

        this.typeClass = typeClass;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        try {
            T instance = typeClass.newInstance();
            instance.deserialize(node);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
