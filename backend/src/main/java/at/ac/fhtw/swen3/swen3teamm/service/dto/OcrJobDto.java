package at.ac.fhtw.swen3.swen3teamm.service.dto;

import java.time.Instant;
import java.util.UUID;

public record OcrJobDto(UUID documentId, String title, Instant createdAt) {}
