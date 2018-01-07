package xqa;

import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import xqa.commons.IngestEvent;
import xqa.commons.MessageSender;
import xqa.commons.XmlFileFinder;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class Ingest {
    private static final Logger logger = LoggerFactory.getLogger(Ingest.class);
    private MessageSender messageSender;
    private String serviceId;
    private String messageBrokerHost;
    private String pathToXmlCandidateFiles;

    public Ingest() {
        serviceId = this.getClass().getSimpleName().toLowerCase() + "/" + UUID.randomUUID().toString().split("-")[0];
        logger.info(serviceId);
    }

    public static void main(String[] args) {
        Signal.handle(new Signal("INT"), signal -> System.exit(1));

        try {
            Ingest ingest = new Ingest();
            ingest.ingestFiles(args);
        } catch (CommandLineException exception) {
            return;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            exception.printStackTrace();
            System.exit(1);
        }
    }

    public void ingestFiles(String[] args) throws Exception {
        Collection<File> xmlFiles = null;
        int sentCounter = 0;

        consumeCommandLine(args);

        messageSender = new MessageSender(messageBrokerHost);

        XmlFileFinder xmlFileFinder = new XmlFileFinder(pathToXmlCandidateFiles);
        xmlFiles = xmlFileFinder.findFiles();

        for (File xmlFile : xmlFiles) {
            try {

                xmlFileFinder.checkFileCanBeUsed(xmlFile);
                sentCounter += 1;

                String correlationId = UUID.randomUUID().toString();
                String xml = xmlFileFinder.contentsOfFile(xmlFile);
                String digest = DigestUtils.sha256Hex(xml);
                int size = xml.getBytes("UTF-8").length;

                logger.info(MessageFormat.format("> {0}: correlationId={1}; digest={2}; size={3} - {4}",
                        String.format("%4d", sentCounter),
                        correlationId,
                        digest,
                        String.format("%12d", size),
                        xmlFile.getPath()));

                sendEventToMessageBroker(new IngestEvent(serviceId, correlationId, xmlFile.getPath(), digest, size, "START"));

                sendXmlToMessageBroker(correlationId, xmlFile.getPath(), xml);

                sendEventToMessageBroker(new IngestEvent(serviceId, correlationId, xmlFile.getPath(), digest, size, "END"));

            } catch (XmlFileFinder.FinderException finderException) {
                logger.debug(finderException.getMessage());
            }
        }

        messageSender.close();

        logger.info(MessageFormat.format("FINISHED - sent: {0}/{1}",
                sentCounter, Optional.ofNullable(xmlFiles.size()).orElse(0)));
    }

    private void consumeCommandLine(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("message_broker_host", true, "i.e. xqa-message-broker");
        options.addOption("path", true, "i.e. /xml");
        CommandLineParser commandLineParser = new DefaultParser();

        CommandLine commandLine = commandLineParser.parse(options, args);

        if (commandLine.hasOption("message_broker_host")) {
            messageBrokerHost = commandLine.getOptionValue("message_broker_host");
            logger.info("message_broker_host=" + messageBrokerHost);
        } else {
            showUsage(options);
        }

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
        messageSender.sendMessage(
                "xqa.db.amqp.insert_event", UUID.randomUUID().toString(), null, new Gson().toJson(ingestEvent));
    }

    private void sendXmlToMessageBroker(String correlationId, final String path, final String xml) throws Exception {
        messageSender.sendMessage("xqa.ingest", correlationId, path, xml);
    }

    @SuppressWarnings("serial")
    public class CommandLineException extends Exception {
        public CommandLineException() {
        }
    }
}
