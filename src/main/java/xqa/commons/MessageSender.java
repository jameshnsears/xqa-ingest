package xqa.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Date;

public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private Connection connection;
    private Session session;

    public MessageSender(String messageBrokerHost) throws Exception {
        ConnectionFactory factory = IngestConnectionFactory.messageBroker(messageBrokerHost);

        int retryAttempts = 3;
        boolean connected = false;
        while (connected == false) {
            try {
                connection = factory.createConnection("admin", "admin");
                connected = true;
            } catch (Exception exception) {
                logger.warn("retryAttempts=" + retryAttempts);
                if (retryAttempts == 0) {
                    throw exception;
                }
                retryAttempts --;
                Thread.sleep(5000);
            }
        }
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    private static BytesMessage constructMessage(Session session,
                                                 Destination ingest,
                                                 String correlationID,
                                                 String subject,
                                                 String body) throws JMSException {
        BytesMessage message = session.createBytesMessage();
        message.setJMSDestination(ingest);
        message.setJMSCorrelationID(correlationID);
        message.setJMSTimestamp(new Date().getTime());

        if (subject != null) {
            message.setJMSType(subject);
        }

        if (body != null) {
            message.writeBytes(body.getBytes());
        }

        return message;
    }

    public void close() throws Exception {
        session.close();
        connection.close();
    }

    public void sendMessage(String destinationName,
                            String correlationID,
                            String subject,
                            String body) throws Exception {
        Destination destination = session.createQueue(destinationName);

        MessageProducer messageProducer = session.createProducer(destination);

        BytesMessage message = constructMessage(session, destination, correlationID, subject, body);
        logger.debug(MessageLogging.log(MessageLogging.Direction.SEND, message));

        messageProducer.send(message, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);

        messageProducer.close();
    }
}
