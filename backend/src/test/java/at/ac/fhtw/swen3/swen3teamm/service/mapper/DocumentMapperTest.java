package at.ac.fhtw.swen3.swen3teamm.service.mapper;

import at.ac.fhtw.swen3.swen3teamm.persistance.DocumentEntity;
import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    //Instanz zum Testen
    private final DocumentMapper mapper = new DocumentMapper();

    @Test
    void toDto_shouldReturnNullForNullEntity() {
        assertThat(mapper.toDto((DocumentEntity) null)).isNull(); //null mit cast auf DocumentEntity
    } //Testet ob Null zurück gibt haha

    @Test
    void toDto_shouldMapEntityToDto() {
        DocumentEntity entity = new DocumentEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setTitle("My Doc");
        entity.setCreatedAt(Instant.now());

        DocumentDto dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(id); //Ob alles korrekt ist
        assertThat(dto.title()).isEqualTo("My Doc");
        assertThat(dto.status()).isEqualTo("UPLOADED");
        assertThat(dto.createdAt()).isEqualTo(entity.getCreatedAt());
    }

    @Test
    void ordering_shouldSortByCreatedAtDescAndTitle() {
        Instant now = Instant.now();

        DocumentDto older = new DocumentDto(UUID.randomUUID(), "B", null, "UPLOADED", now.minusSeconds(60), null);
        DocumentDto newer = new DocumentDto(UUID.randomUUID(), "A", null, "UPLOADED", now, null);

        List<DocumentDto> sorted = mapper.ordering(List.of(older, newer));

        assertThat(sorted).containsExactly(newer, older); //Überprüft Reihenfolge
    }
}
