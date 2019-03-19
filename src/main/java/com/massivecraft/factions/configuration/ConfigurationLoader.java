package com.massivecraft.factions.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class ConfigurationLoader {
    private final ObjectMapper objectMapper;
    private final File file;

    public <T extends IConfigurable> T to(Class<T> configurationClass) throws Exception {
        if (!file.exists()) {
            return configurationClass.newInstance();
        }

        return objectMapper.readValue(file, configurationClass);
    }
}
