package xqa.integration;

import org.junit.jupiter.api.Test;
import xqa.ingest.Ingest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IngestTest {
    private String getResource() {
        return Thread.currentThread().getContextClassLoader().getResource("test-data").getPath();
    }

    @Test
    void ingestMain() throws Exception {
        assertEquals(2, Ingest.executeIngest((new String[]{"-message_broker_host", "127.0.0.1", "-path", getResource()})));
    }

    @Test
    void ingestShowUsage() {
        assertThrows(Ingest.CommandLineException.class,
                () -> {
                    Ingest.main(new String[]{});
                });

        assertThrows(Ingest.CommandLineException.class,
                () -> {
                    Ingest.main(new String[]{"-message_broker_host", "127.0.0.1"});
                });

        assertThrows(Ingest.CommandLineException.class,
                () -> {
                    Ingest.main(new String[]{"-path", getResource()});
                });
    }
}