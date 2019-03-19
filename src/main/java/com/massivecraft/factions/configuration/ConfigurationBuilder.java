package com.massivecraft.factions.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.massivecraft.factions.configuration.deserialize.ItemStackDeserializer;
import com.massivecraft.factions.configuration.serialize.ItemStackSerializer;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class ConfigurationBuilder {
    public static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new SimpleModule()
                    .addSerializer(ItemStack.class, new ItemStackSerializer())
                    .addDeserializer(ItemStack.class, new ItemStackDeserializer()));

    public static ConfigurationBuilder instance;

    public ConfigurationLoader from(File file) {
        return new ConfigurationLoader(objectMapper, file);
    }

    public ConfigurationLoader from(String file) {
        return from(new File(file));
    }

    public ConfigurationSaver from(IConfigurable configurable) {
        return new ConfigurationSaver(objectMapper, configurable);
    }

    public static ConfigurationBuilder getInstance() {
        return instance == null ? (instance = new ConfigurationBuilder()) : instance;
    }
}
