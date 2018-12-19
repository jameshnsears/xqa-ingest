package xqa.ingest;

import java.util.Date;

class IngestEvent {
    public final String serviceId;
    public final long creationTime;
    public final String correlationId;
    public final String source;
    public final String digest;
    public final int size;
    public final String state;

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
