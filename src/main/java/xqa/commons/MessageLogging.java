package xqa.commons;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsBytesMessageFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class MessageLogging {
    private static final Logger logger = LoggerFactory.getLogger(MessageLogging.class);

    public static String log(Direction direction, Message message) throws Exception {
        return formmatedLog(getArrow(direction),
                Long.toString(message.getJMSTimestamp()),
                message.getJMSCorrelationID(),
                message.getJMSDestination(),
                message.getJMSReplyTo(),
                getSubject(message),
                message.getJMSExpiration(),
                getTextFromMessage(message));
    }

    private static String getSubject(Message message) {
        logger.debug("JmsBytesMessage");
        JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) message;
        AmqpJmsBytesMessageFacade facade = (AmqpJmsBytesMessageFacade) jmsBytesMessage.getFacade();
        return facade.getType();
    }

    private static String getTextFromMessage(Message message) throws JMSException, UnsupportedEncodingException {
        logger.debug("JmsBytesMessage");
        JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) message;
        jmsBytesMessage.reset();
        byte[] byteData;
        byteData = new byte[(int) jmsBytesMessage.getBodyLength()];
        jmsBytesMessage.readBytes(byteData);
        return new String(byteData, "UTF-8");
    }

    private static String formmatedLog(String arrowDirection, String jmsTimestamp, String jmsCorrelationID,
                                       Destination jmsDestination, Destination jmsReplyTo, String subject,
                                       long jmsExpiration, String text) {
        return MessageFormat.format(
                "{0} jmsTimestamp={1}; jmsCorrelationID={2}; jmsDestination={3}; jmsReplyTo={4}; subject={5}; jmsExpiration={6}; digest(text)={7}",
                arrowDirection, jmsTimestamp, jmsCorrelationID, jmsDestination, jmsReplyTo, subject, jmsExpiration, DigestUtils.sha256Hex(text));
    }

    private static String getArrow(Direction direction) {
        return "<";
    }

    public enum Direction {
        SEND
    }
}
