package dev.nikkune.msnotes.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PatientClientTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    private PatientClient newClient() {
        String baseUrl = server.url("/exists").toString();
        return new PatientClient(baseUrl, "user", "pass");
        // baseUrl will be appended with query params by PatientClient
    }

    @Test
    void exists_returnsTrue_on2xx() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));
        PatientClient client = newClient();

        boolean result = client.exists("John", "Doe");

        assertTrue(result);
        RecordedRequest req = server.takeRequest();
        assertTrue(req.getPath().startsWith("/exists"));
        String auth = req.getHeader("Authorization");
        assertNotNull(auth);
        assertTrue(auth.startsWith("Basic "));
    }

    @Test
    void exists_returnsFalse_on404() {
        server.enqueue(new MockResponse().setResponseCode(404));
        PatientClient client = newClient();

        boolean result = client.exists("Ghost", "User");

        assertFalse(result);
    }

    @Test
    void exists_throwsRuntime_on401() {
        server.enqueue(new MockResponse().setResponseCode(401));
        PatientClient client = newClient();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> client.exists("Bad", "Auth"));
        assertTrue(ex.getMessage().toLowerCase().contains("authenticate"));
    }

    @Test
    void exists_propagatesOther4xx() {
        server.enqueue(new MockResponse().setResponseCode(400));
        PatientClient client = newClient();

        // 400 should not be converted to boolean or RuntimeException; it should propagate
        assertThrows(org.springframework.web.client.HttpClientErrorException.BadRequest.class,
                () -> client.exists("A", "B"));
    }
}
