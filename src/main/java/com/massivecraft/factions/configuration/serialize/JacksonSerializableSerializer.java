package com.massivecraft.factions.configuration.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class JacksonSerializableSerializer extends StdSerializer<JacksonSerializable> {

    public JacksonSerializableSerializer() {
        super(JacksonSerializable.class);
    }

    @Override
    public void serialize(JacksonSerializable serializable, JsonGenerator generator, SerializerProvider provider) throws IOException {
        serializable.serialize(generator);
    }
}
