package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccessLogXmlParserTest {

    @TempDir
    Path tempDir;

    @Test
    void parse_validXml_parsesHeaderAndOneEntry() throws Exception {
        UUID id = UUID.randomUUID();

        String xml = """
                <access-log date="2025-12-15" sourceSystem="pdfFileManager">
                  <entry>
                    <documentId>%s</documentId>
                    <count>7</count>
                  </entry>
                </access-log>
                """.formatted(id);

        Path file = tempDir.resolve("accesslog.xml");
        Files.writeString(file, xml);

        AccessLogXmlParser parser = new AccessLogXmlParser();
        AccessLogFile logFile = parser.parse(file);

        assertEquals("2025-12-15", logFile.getDate());
        assertEquals("pdfFileManager", logFile.getSourceSystem());
        assertEquals(1, logFile.getEntries().size());
        assertEquals(id, logFile.getEntries().get(0).getDocumentId());
        assertEquals(7, logFile.getEntries().get(0).getCount());
    }

    @Test
    void parse_multipleEntries_parsesAll() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        String xml = """
                <access-log date="2025-12-15" sourceSystem="pdfFileManager">
                  <entry><documentId>%s</documentId><count>1</count></entry>
                  <entry><documentId>%s</documentId><count>2</count></entry>
                </access-log>
                """.formatted(id1, id2);

        Path file = tempDir.resolve("accesslog2.xml");
        Files.writeString(file, xml);

        AccessLogXmlParser parser = new AccessLogXmlParser();
        AccessLogFile logFile = parser.parse(file);

        assertEquals(2, logFile.getEntries().size());
        assertEquals(id1, logFile.getEntries().get(0).getDocumentId());
        assertEquals(1, logFile.getEntries().get(0).getCount());
        assertEquals(id2, logFile.getEntries().get(1).getDocumentId());
        assertEquals(2, logFile.getEntries().get(1).getCount());
    }

    @Test
    void parse_invalidXml_throwsException() throws Exception {
        Path file = tempDir.resolve("broken.xml");
        Files.writeString(file, "<access-log><entry></access-log>");

        AccessLogXmlParser parser = new AccessLogXmlParser();
        assertThrows(Exception.class, () -> parser.parse(file));
    }
}
