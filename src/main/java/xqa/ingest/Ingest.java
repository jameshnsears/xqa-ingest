package xqa.ingest;

import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import xqa.commons.qpid.jms.MessageBroker;
import xqa.commons.qpid.jms.MessageMaker;

import javax.jms.Message;
import java.io.File;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;

public class Ingest {
    private static final Logger logger = LoggerFactory.getLogger(Ingest.class);
    private final String serviceId;

    private MessageBroker messageBroker;

    private String messageBrokerHost;
    private int messageBrokerPort;
    private String messageBrokerUsername;
    private String messageBrokerPassword;
    private int messageBrokerRetryAttempts;

    private String destinationIngest;
    private String destinationEvent;

    private String pathToXmlCandidateFiles;

    public Ingest() {
        serviceId = this.getClass().getSimpleName().toLowerCase() + "/" + UUID.randomUUID().toString().split("-")[0];
        logger.info(serviceId);
    }

    public static int main(String[] args) throws Exception {
        Signal.handle(new Signal("INT"), signal -> System.exit(1));

        try {
            Ingest ingest = new Ingest();
            ingest.processCommandLine(args);
            return ingest.ingestFiles();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw exception;
        }
    }

    public int ingestFiles() throws Exception {
        messageBroker = new MessageBroker(
                messageBrokerHost,
                messageBrokerPort,
                messageBrokerUsername,
                messageBrokerPassword,
                messageBrokerRetryAttempts);

        int sentCounter = 0;

        XmlFileFinder xmlFileFinder = new XmlFileFinder(pathToXmlCandidateFiles);
        for (File xmlFile : xmlFileFinder.findFiles()) {
            try {
                xmlFileFinder.checkFileCanBeUsed(xmlFile);
                sentCounter += 1;

                sendXmlFileToMessageBroker(sentCounter, xmlFileFinder, xmlFile);

            } catch (XmlFileFinder.FinderException finderException) {
                logger.debug(finderException.getMessage());
            }
        }

        messageBroker.close();

        logger.info(MessageFormat.format("FINISHED - sent: {0}/{1}",
                sentCounter, Optional.of(xmlFileFinder.findFiles().size()).orElse(0)));

        return sentCounter;
    }

    private void sendXmlFileToMessageBroker(int sentCounter, XmlFileFinder xmlFileFinder, File xmlFile) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        String xml = xmlFileFinder.contentsOfFile(xmlFile);
        String digest = DigestUtils.sha256Hex(xml);
        int size = xml.getBytes("UTF-8").length;

        logger.info(MessageFormat.format("Y: {0}: correlationId={1}; digest={2}; size={3} - {4}",
                String.format("%4d", sentCounter),
                correlationId,
                digest,
                String.format("%12d", size),
                xmlFile.getPath()));

        sendEventToMessageBroker(new IngestEvent(serviceId, correlationId, xmlFile.getPath(), digest, size, "START"));

        sendXmlToMessageBroker(correlationId, xmlFile.getPath(), xml);

        sendEventToMessageBroker(new IngestEvent(serviceId, correlationId, xmlFile.getPath(), digest, size, "END"));
    }

    public void processCommandLine(String[] args) throws ParseException, CommandLineException {
        Options options = new Options();

        options.addOption("message_broker_host", true, "i.e. xqa-message-broker");
        options.addOption("message_broker_port", true, "i.e. 5672");
        options.addOption("message_broker_username", true, "i.e. admin");
        options.addOption("message_broker_password", true, "i.e. admin");
        options.addOption("message_broker_retry", true, "i.e. 3");

        options.addOption("destination_ingest", true, "i.e. xqa.ingest");
        options.addOption("destination_event", true, "i.e. xqa.event");

        options.addOption("path", true, "i.e. /xml");

        CommandLineParser commandLineParser = new DefaultParser();
        setConfigurationValues(options, commandLineParser.parse(options, args));
    }

    private void setConfigurationValues(Options options, CommandLine commandLine) throws CommandLineException {
        if (commandLine.hasOption("message_broker_host")) {
            messageBrokerHost = commandLine.getOptionValue("message_broker_host");
            logger.info("message_broker_host=" + messageBrokerHost);
        } else {
            showUsage(options);
        }

        messageBrokerPort = Integer.parseInt(commandLine.getOptionValue("message_broker_port", "5672"));
        messageBrokerUsername = commandLine.getOptionValue("message_broker_username", "admin");
        messageBrokerPassword = commandLine.getOptionValue("message_broker_password", "admin");
        messageBrokerRetryAttempts = Integer.parseInt(commandLine.getOptionValue("message_broker_retry", "3"));

        destinationIngest = commandLine.getOptionValue("destination_ingest", "xqa.ingest");
        destinationEvent = commandLine.getOptionValue("destination_event", "xqa.event");

        if (commandLine.hasOption("path")) {
            pathToXmlCandidateFiles = commandLine.getOptionValue("path");
            logger.info("path=" + pathToXmlCandidateFiles);
        } else {
            showUsage(options);
        }
    }

    private void showUsage(final Options options) throws CommandLineException {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("Ingest", options);
        throw new Ingest.CommandLineException();
    }

    private void sendEventToMessageBroker(final IngestEvent ingestEvent) throws Exception {
        Message message = MessageMaker.createMessage(
                messageBroker.getSession(),
                messageBroker.getSession().createQueue(destinationEvent),
                UUID.randomUUID().toString(),
                new Gson().toJson(ingestEvent));

        messageBroker.sendMessage(message);
    }

    private void sendXmlToMessageBroker(String correlationId, final String path, final String xml) throws Exception {
        Message message = MessageMaker.createMessage(
                messageBroker.getSession(),
                messageBroker.getSession().createQueue(destinationIngest),
                correlationId,
                path,
                xml);

        messageBroker.sendMessage(message);
    }

    @SuppressWarnings("serial")
    public class CommandLineException extends Exception {
        public CommandLineException() {
        }
    }
}
