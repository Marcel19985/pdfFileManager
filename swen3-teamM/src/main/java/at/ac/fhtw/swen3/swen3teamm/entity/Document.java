package at.ac.fhtw.swen3.swen3teamm.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class Document {
    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    //getter und setter:
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
