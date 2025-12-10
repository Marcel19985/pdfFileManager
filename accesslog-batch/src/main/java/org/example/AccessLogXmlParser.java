package org.example;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AccessLogXmlParser {

    private final XmlMapper xmlMapper = new XmlMapper();

    public AccessLogFile parse(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return xmlMapper.readValue(in, AccessLogFile.class);
        }
    }
}
