package at.ac.fhtw.swen3.swen3teamm.service.mapper;

import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import at.ac.fhtw.swen3.swen3teamm.persiatance.DocumentEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class DocumentMapper extends AbstractMapper<DocumentEntity, DocumentDto> {
    @Override
    public DocumentDto toDto(DocumentEntity document) {
        if (document == null) {
            return null;
        }
        return new DocumentDto(
                document.getId(),
                document.getTitle(),
                "UPLOADED",
                document.getCreatedAt()
        );
    }

    @Override
    List<DocumentDto> ordering(List<DocumentDto> dtos) {
        // Neueste zuerst; bei gleichem Datum nach Titel
        return dtos.stream()
                .sorted(Comparator
                        .comparing(DocumentDto::createdAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed()
                        .thenComparing(DocumentDto::title, Comparator.nullsLast(Comparator.naturalOrder()))
                )
                .toList();
    }
}
