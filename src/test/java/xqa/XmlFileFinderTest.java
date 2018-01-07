package xqa;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.rules.TemporaryFolder;
import xqa.commons.XmlFileFinder;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@EnableRuleMigrationSupport
public class XmlFileFinderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private URL getResource(final String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResource(resourceName);
    }

    @Test
    void findXmlFilesInPath() throws Exception {
        XmlFileFinder xmlFileFinder = new XmlFileFinder(getResource("test-data").getPath());
        assertEquals(xmlFileFinder.findFiles().size(), 5);
    }

    @Test
    void rmBomFromFile() throws Exception {
        FileUtils.copyFileToDirectory(
                new File(getResource("test-data/bad/bom/with_bom.xml").getFile()),
                temporaryFolder.getRoot());

        XmlFileFinder xmlFileFinder = new XmlFileFinder(temporaryFolder.getRoot().getPath());
        for (File candidateXmlfile : xmlFileFinder.findFiles())
            xmlFileFinder.rmBomFromFileContents(candidateXmlfile);

        assertTrue(FileUtils.contentEquals(
                Paths.get(temporaryFolder.getRoot().toString(), "with_bom.xml").toFile(),
                new File(getResource("test-data/bad/bom/without_bom.xml").getFile())));
    }

    @Test
    void noXmlFilesFound() {
        XmlFileFinder.FinderException exception = assertThrows(XmlFileFinder.FinderException.class,
                () -> {
                    XmlFileFinder xmlFileFinder = new XmlFileFinder(temporaryFolder.getRoot().getPath());
                    xmlFileFinder.findFiles();
                });
        assertEquals(XmlFileFinder.ERROR_NO_XML_FILES_FOUND, exception.getMessage());
    }

    @Test
    void fileContentsNotWellFormed() {
        XmlFileFinder.FinderException exception = assertThrows(XmlFileFinder.FinderException.class,
                () -> {
                    XmlFileFinder xmlFileFinder = new XmlFileFinder();
                    xmlFileFinder.checkFileCanBeUsed(new File(getResource("test-data/bad/not_well_formed.xml").getPath()));
                });
        assertEquals(XmlFileFinder.ERROR_FILE_CONTENTS_NOT_WELL_FORMED, exception.getMessage());
    }

    @Test
    void fileContentsWellFormed() throws Exception {
        XmlFileFinder xmlFileFinder = new XmlFileFinder();
        xmlFileFinder.checkFileCanBeUsed(new File(getResource("test-data/well_formed.xml").getPath()));
    }

    @Test
    void fileHasWrongMimeType() {
        assertThrows(XmlFileFinder.FinderException.class,
                () -> {
                    XmlFileFinder xmlFileFinder = new XmlFileFinder();
                    xmlFileFinder.checkFileCanBeUsed(new File(getResource("test-data/bad/mime_type.xml").getPath()));
                });
    }

    @Test
    void contentsOfFile() throws Exception {
        XmlFileFinder xmlFileFinder = new XmlFileFinder();
        assertEquals("<test>Ã¥</test>",
                xmlFileFinder.contentsOfFile(new File(getResource("test-data/well_formed.xml").getPath())));
    }
}
