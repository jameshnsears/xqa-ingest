package xqa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.rules.TemporaryFolder;

import xqa.ingest.XmlFileFinder;

@EnableRuleMigrationSupport
public class XmlFileFinderTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private URL getResource(final String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResource(resourceName);
    }

    @Test
    void findXmlFilesInPath() throws Exception {
        XmlFileFinder xmlFileFinder = new XmlFileFinder(getResource("test-data").getPath());
        assertEquals(xmlFileFinder.findFiles().size(), 3);
    }

    @Test
    void rmBomFromFile() throws Exception {
        FileUtils.copyFileToDirectory(
                new File(getResource("test-data/bad/bom/with_bom.xml.gz").getFile()),
                temporaryFolder.getRoot());

        // preserves BOM
        decompressGzipFile(
                temporaryFolder.getRoot().getPath().concat("/with_bom.xml.gz"),
                temporaryFolder.getRoot().getPath().concat("/with_bom.xml"));

        XmlFileFinder xmlFileFinder = new XmlFileFinder(temporaryFolder.getRoot().getPath());
        for (File candidateXmlfile : xmlFileFinder.findFiles())
            xmlFileFinder.rmBomFromFileContents(candidateXmlfile);

        assertTrue(FileUtils.contentEquals(
                Paths.get(temporaryFolder.getRoot().toString(), "with_bom.xml").toFile(),
                new File(getResource("test-data/bad/bom/without_bom.xml").getFile())));
    }

    private void decompressGzipFile(String gzipFile, String newFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(gzipFile);
        GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
        FileOutputStream fileOutputStream = new FileOutputStream(newFile);

        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, len);
        }

        fileOutputStream.close();
        gzipInputStream.close();
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
                    xmlFileFinder.checkFileCanBeUsed(new File(getResource("test-data/bad/mime_type.txt").getPath()));
                });
    }

    @Test
    void contentsOfFile() throws Exception {
        XmlFileFinder xmlFileFinder = new XmlFileFinder();
        assertEquals("<test>Ã¥</test>",
                xmlFileFinder.contentsOfFile(new File(getResource("test-data/well_formed.xml").getPath())));
    }
}
