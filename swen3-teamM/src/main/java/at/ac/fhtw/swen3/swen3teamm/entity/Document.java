package at.ac.fhtw.swen3.swen3teamm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Document {
    //getter und setter:
    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
