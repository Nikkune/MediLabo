package dev.nikkune.msrisk.client;

import dev.nikkune.msrisk.dto.NoteDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Client class for interacting with the Notes microservice. This class provides
 * functionality to check the existence of a note based on their first and last name.
 * It uses a REST client to make requests to the configured note service API.
 */
@Component
public class NotesClient {
    private static final Logger logger = LogManager.getLogger(NotesClient.class);

    private final RestClient restClient;
    private final String baseUrl;
    private final String authHeader;

    /**
     * Constructs a NotesClient instance for interacting with the Notes microservice.
     * It initializes the base URL, authentication header, and REST client for making API requests.
     *
     * @param baseUrl the base URL of the Notes microservice, loaded from application properties.
     * @param username the username for Basic authentication with the Notes microservice. Defaults to "medilabo".
     * @param password the password for Basic authentication with the Notes microservice. Defaults to "medilabo123".
     */
    public NotesClient(
            @Value("${ms.notes.base-url}") String baseUrl,
            @Value("${ms.notes.username:medilabo}") String username,
            @Value("${ms.notes.password:medilabo123}") String password) {
        this.baseUrl = baseUrl;

        // Créer l'en-tête Basic Auth
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        this.authHeader = "Basic " + encodedCredentials;

        this.restClient = RestClient.create();
    }

    public List<String> allPatientNotes(String firstName, String lastName) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("firstName", firstName)
                .queryParam("lastName", lastName)
                .toUriString();
        try {
            ResponseEntity<List<NoteDTO>> response = restClient.get()
                    .uri(url)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<NoteDTO>>() {});

            List<NoteDTO> notes = response.getBody();
            if (notes == null) {
                logger.warn("No notes found for {} {}", firstName, lastName);
                throw new RuntimeException("No notes found");
            }
            return notes.stream().map(NoteDTO::getNote).toList();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("No notes found for {} {}", firstName, lastName);
            throw new RuntimeException("No notes found", e);
        } catch (HttpClientErrorException e) {
            logger.error("Error when retrieving notes: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error when retrieving notes: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve notes", e);
        }
    }
}
