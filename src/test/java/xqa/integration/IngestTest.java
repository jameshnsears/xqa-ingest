package xqa.integration;

import org.junit.jupiter.api.Test;
import xqa.Ingest;

import static org.junit.jupiter.api.Assertions.assertThrows;

class IngestTest {
    private String getResource() {
        return Thread.currentThread().getContextClassLoader().getResource("test-data").getPath();
    }

    @Test
    void ingestTestData() throws Exception {
        Ingest ingest = new Ingest();
        ingest.ingestFiles(new String[]{"-message_broker_host", "127.0.0.1", "-path", getResource()});
    }

    @Test
    void ingestShowUsage() throws Exception {
        assertThrows(Ingest.CommandLineException.class,
                () -> {
                    Ingest ingest = new Ingest();
                    ingest.ingestFiles(new String[]{"-message_broker_host", "127.0.0.1"});
                });

        assertThrows(Ingest.CommandLineException.class,
                () -> {
                    Ingest ingest = new Ingest();
                    ingest.ingestFiles(new String[]{"-path", getResource()});
                });
    }
}