package at.ac.fhtw.swen3.swen3teamm.service.dto;

import java.time.Instant;

public record GenAiResultDto(
        String documentId,
        String summary,
        String model,
        Integer tokens,
        Instant createdAt,
        String category,
        String error
) {}
