package dev.nikkune.msrisk.client;

import dev.nikkune.msrisk.dto.RiskDTO;
import dev.nikkune.msrisk.exception.PatientNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Client class for interacting with the Patient microservice. This class provides
 * functionality to check the existence of a patient based on their first and last name.
 * It uses a REST client to make requests to the configured patient service API.
 */
@Component
public class PatientClient {
    private static final Logger logger = LogManager.getLogger(PatientClient.class);

    private final RestClient restClient;
    private final String baseUrl;
    private final String authHeader;

    /**
     * Constructs a PatientClient instance for interacting with the Patient microservice.
     * It initializes the base URL, authentication header, and REST client for making API requests.
     *
     * @param baseUrl  the base URL of the Patient microservice, loaded from application properties.
     * @param username the username for Basic authentication with the Patient microservice. Defaults to "medilabo".
     * @param password the password for Basic authentication with the Patient microservice. Defaults to "medilabo123".
     */
    public PatientClient(
            @Value("${ms.patient.base-url}") String baseUrl,
            @Value("${ms.patient.username:medilabo}") String username,
            @Value("${ms.patient.password:medilabo123}") String password) {
        this.baseUrl = baseUrl;

        // Créer l'en-tête Basic Auth
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        this.authHeader = "Basic " + encodedCredentials;

        this.restClient = RestClient.create();
    }

    public RiskDTO getPatientRiskInfo(String firstName, String lastName) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/riskInfo")
                .queryParam("firstName", firstName)
                .queryParam("lastName", lastName)
                .toUriString();
        try {
            ResponseEntity<RiskDTO> response = restClient.get()
                    .uri(url)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .toEntity(RiskDTO.class);

            RiskDTO patient = response.getBody();
            if (patient == null) {
                logger.error("Patient not found for {} {}", firstName, lastName);
                throw new PatientNotFoundException(firstName, lastName);
            }
            return patient;
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Patient not found for {} {}", firstName, lastName);
            throw new PatientNotFoundException(firstName, lastName);
        } catch (HttpClientErrorException e) {
            logger.error("Error when checking patient existence: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error when checking patient existence: {}", e.getMessage());
            throw new RuntimeException("Failed to check patient existence", e);
        }
    }
}
