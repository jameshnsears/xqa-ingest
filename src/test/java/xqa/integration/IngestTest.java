package xqa.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jameshnsears.configuration.ConfigurationAccessor;
import com.github.jameshnsears.configuration.ConfigurationParameterResolver;
import com.github.jameshnsears.docker.DockerClient;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import xqa.ingest.Ingest;

@ExtendWith(ConfigurationParameterResolver.class)
class IngestTest {
    private String getResource() {
        return Thread.currentThread().getContextClassLoader().getResource("test-data").getPath();
    }

    @Test
    void ingestMain(final ConfigurationAccessor configurationAccessor) throws Exception {
        DockerClient dockerClient = new DockerClient();

        try {
            dockerClient.pull(configurationAccessor.images());
            dockerClient.startContainers(configurationAccessor);

            assertEquals(
                    2,
                    Ingest.executeIngest((new String[]{"-message_broker_host", "127.0.0.1", "-path", getResource()})));
        } finally {
            dockerClient.rmContainers(configurationAccessor);
        }
    }

    @Test
    void ingestShowUsage() {
        assertThrows(Ingest.CommandLineException.class,
                () -> Ingest.main(new String[]{}));

        assertThrows(Ingest.CommandLineException.class,
                () -> Ingest.main(new String[]{"-message_broker_host", "127.0.0.1"}));

        assertThrows(Ingest.CommandLineException.class,
                () -> Ingest.main(new String[]{"-path", getResource()}));
    }
}