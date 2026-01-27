package org.example;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

//XML File einlesen und in Java Objekt umwandeln
@Service
public class AccessLogXmlParser {

    private final XmlMapper xmlMapper = new XmlMapper(); //Jackson XML Mapper (so wie ObjectMapper für JSON, nur für XML)

    public AccessLogFile parse(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return xmlMapper.readValue(in, AccessLogFile.class); //XML zu Java Objekt umwandeln
        }
    }
}
