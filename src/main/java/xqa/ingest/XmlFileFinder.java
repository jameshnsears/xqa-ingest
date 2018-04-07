package xqa.ingest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collection;

public class XmlFileFinder {
    public static final String ERROR_NO_XML_FILES_FOUND = "no XML files found";
    public static final String ERROR_FILE_MIMETYPE = "incorrect mimetype";
    public static final String ERROR_FILE_CONTENTS_NOT_WELL_FORMED = "file not well-formed";

    private static final Logger logger = LoggerFactory.getLogger(XmlFileFinder.class);
    private String pathToXmlCandidateFiles;

    public XmlFileFinder() {
    }

    public XmlFileFinder(final String pathToXmlCandidateFiles) {
        this.pathToXmlCandidateFiles = pathToXmlCandidateFiles;
    }

    public Collection<File> findFiles() throws FinderException {
        Collection<File> candidateXmlFiles = FileUtils.listFiles(new File(pathToXmlCandidateFiles),
                new String[]{"xml", "XML"}, true);

        if (candidateXmlFiles.size() == 0)
            throw new XmlFileFinder.FinderException(XmlFileFinder.ERROR_NO_XML_FILES_FOUND);

        logger.info(
                MessageFormat.format("found {0} file(s) in {1}", candidateXmlFiles.size(), pathToXmlCandidateFiles));

        return candidateXmlFiles;
    }

    public void rmBomFromFileContents(File candidateXmlfile) throws Exception {
        // grep -rl $'\xEF\xBB\xBF' .
        InputStream fileInputStream = new FileInputStream(candidateXmlfile);
        BOMInputStream bomInputStream = new BOMInputStream(fileInputStream);

        File tmpFile = null;
        if (bomInputStream.hasBOM()) {
            logger.debug(MessageFormat.format("removing BOM: {0}", candidateXmlfile.getCanonicalPath()));

            tmpFile = new File(candidateXmlfile.getPath().concat(".tmp"));
            OutputStream fileOutputStream = new FileOutputStream(tmpFile);
            IOUtils.copy(bomInputStream, fileOutputStream);
            fileOutputStream.close();
        }
        bomInputStream.close();

        if (tmpFile != null) {
            candidateXmlfile.delete();
            tmpFile.renameTo(candidateXmlfile);
        }
    }

    public void checkFileCanBeUsed(File candidateXmlFile) throws Exception {
        if (!checkFileMimeTypeRecognised(candidateXmlFile)) {
            logger.warn(
                    MessageFormat.format("N: {0}: {1}", XmlFileFinder.ERROR_FILE_MIMETYPE, candidateXmlFile.getPath()));
            throw new FinderException(XmlFileFinder.ERROR_FILE_MIMETYPE);
        }

        if (checkFileContentsWellFormed(candidateXmlFile) == Boolean.FALSE) {
            logger.warn(MessageFormat.format("N: {0}: {1}", XmlFileFinder.ERROR_FILE_CONTENTS_NOT_WELL_FORMED,
                    candidateXmlFile.getPath()));
            throw new XmlFileFinder.FinderException(XmlFileFinder.ERROR_FILE_CONTENTS_NOT_WELL_FORMED);
        }

        rmBomFromFileContents(candidateXmlFile);
        logger.debug(MessageFormat.format("Y: {0}", candidateXmlFile.getPath()));
    }

    private Boolean checkFileContentsWellFormed(File candidateXmlFile) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setValidating(false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            documentBuilder.parse(candidateXmlFile);
        } catch (Exception exception) {
            logger.debug(exception.getMessage());
            return false;
        }
        return true;
    }

    private Boolean checkFileMimeTypeRecognised(File candidateXmlFile) throws IOException {
        String mimeType = Files.probeContentType(Paths.get(candidateXmlFile.getPath()));
        logger.debug(mimeType);

        return (mimeType.equals("application/xml") || mimeType.equals("text/xml"));
    }

    public String contentsOfFile(File xmlFile) throws Exception {
        return FileUtils.readFileToString(xmlFile, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("serial")
    public class FinderException extends Exception {
        FinderException(String message) {
            super(message);
        }
    }
}
