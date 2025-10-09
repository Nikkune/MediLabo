package dev.nikkune.msrisk.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class TriggerWordsProvider {
    private static final String RESSOURCE_PATH = "/triggerWord.json";

    @Bean
    public List<String> triggerWords() {
        try (InputStream is = getClass().getResourceAsStream(RESSOURCE_PATH)) {
            if (is == null) {
                throw new RuntimeException("Trigger words file not found");
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to read trigger words file", e);
        }
    }
}
