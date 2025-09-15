package at.ac.fhtw.swen3.swen3teamm.dto;

import java.time.Instant;
import java.util.UUID;

//DTO: data transfer object -> reines Datenobjekt, keine Logik: wird verwendet, um Daten zwischen verschiedenen Schichten der Anwendung zu übertragen; Vorteil: Model Objekte sind dirket an Datenbank gekoppelt und dadurch auch properties, die user nicht sehen sollte. DTO enthält nur das, was user sehen soll
//seit Java 16 record: generiert alles, was man für ein Datenobjekt benötigt: Konstruktur, getter, setter toString, equals, hashCode -> weniger Boilerplate Code
public record DocumentCreatedDto(
        UUID id,
        String title,
        String status,
        Instant createdAt
) {}
