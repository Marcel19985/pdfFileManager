package org.example;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class AccessEntry {

    @JacksonXmlProperty(localName = "documentId")
    private UUID documentId;

    @JacksonXmlProperty(localName = "count")
    private int count;
}