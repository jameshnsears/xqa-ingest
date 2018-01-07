package xqa.commons;

import java.util.Date;

public class IngestEvent {
    private String serviceId;
    private long creationTime;
    private String correlationId;
    private String source;
    private String digest;
    private int size;
    private String state;

    public IngestEvent(final String serviceId,
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
