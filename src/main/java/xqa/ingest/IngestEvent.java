package xqa.ingest;

import java.util.Date;

class IngestEvent {
    private final String serviceId;
    private final long creationTime;
    private final String correlationId;
    private final String source;
    private final String digest;
    private final int size;
    private final String state;

    IngestEvent(final String serviceId,
                final String correlationId,
                final String source,
                final String digest,
                final int size,
                final String state) {
        this.serviceId = serviceId;
        this.creationTime = new Date().getTime();
        this.correlationId = correlationId;
        this.source = source;
        this.digest = digest;
        this.size = size;
        this.state = state;
    }
}
