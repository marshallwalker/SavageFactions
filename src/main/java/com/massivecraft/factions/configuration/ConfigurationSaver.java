package com.massivecraft.factions.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public class ConfigurationSaver {
    private final ObjectMapper objectMapper;
    private final IConfigurable configurable;

    public void to(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        objectMapper
                .writer()
                .withDefaultPrettyPrinter()
                .writeValue(file, configurable);
    }

    public void to(String filename) throws IOException {
        to(new File(filename));
    }
}
