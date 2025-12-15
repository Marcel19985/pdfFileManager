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
    void parse_validXml_returnsAccessLogFile() throws Exception {
        UUID id = UUID.randomUUID();

        String xml = """
                <access-log date="2025-12-15" sourceSystem="pdfFileManager">
                  <entry>
                    <documentId>%s</documentId>
                    <count>3</count>
                  </entry>
                </access-log>
                """.formatted(id);

        Path file = tempDir.resolve("accesslog.xml");
        Files.writeString(file, xml);

        AccessLogXmlParser parser = new AccessLogXmlParser();
        AccessLogFile result = parser.parse(file);

        assertNotNull(result);
        assertEquals("2025-12-15", result.getDate());
        assertEquals("pdfFileManager", result.getSourceSystem());
        assertEquals(1, result.getEntries().size());
        assertEquals(id, result.getEntries().get(0).getDocumentId());
        assertEquals(3, result.getEntries().get(0).getCount());
    }
}
