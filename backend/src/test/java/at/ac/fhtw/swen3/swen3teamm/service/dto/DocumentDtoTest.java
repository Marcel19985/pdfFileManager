package at.ac.fhtw.swen3.swen3teamm.service.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentDtoTest {

    @Test
    void equalsAndHashCode_shouldWorkForSameValues() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        //zwei Dokumente mit exakt den gleichen Werten
        DocumentDto dto1 = new DocumentDto(id, "Doc", null, "UPLOADED", now, null);
        DocumentDto dto2 = new DocumentDto(id, "Doc", null, "UPLOADED", now, null);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode()); //sind die gleichen Objekte wenn sie gleichen HashCode haben
        //wenn man beide in ein HashSet gibt, ist nur eines drinnen
    }


}
