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
    void ingestTestData() throws Exception {
        Ingest ingest = new Ingest();
        ingest.processCommandLine(new String[]{"-message_broker_host", "127.0.0.1", "-path", getResource()});
        assertEquals(3, ingest.ingestFiles());
    }

    @Test
    void ingestShowUsage() {
        assertThrows(Ingest.CommandLineException.class,
                () -> {
                    Ingest ingest = new Ingest();
                    ingest.processCommandLine(new String[]{"-message_broker_host", "127.0.0.1"});
                });

        assertThrows(Ingest.CommandLineException.class,
                () -> {
                    Ingest ingest = new Ingest();
                    ingest.processCommandLine(new String[]{"-path", getResource()});
                });
    }
}