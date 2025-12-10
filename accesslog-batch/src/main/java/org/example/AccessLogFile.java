package org.example;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "access-log")
@Getter
@Setter
public class AccessLogFile {

    @JacksonXmlProperty(isAttribute = true, localName = "date")
    private String date; // später in LocalDate geparst

    @JacksonXmlProperty(isAttribute = true, localName = "sourceSystem")
    private String sourceSystem;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "entry")
    private List<AccessEntry> entries = new ArrayList<>();
}

