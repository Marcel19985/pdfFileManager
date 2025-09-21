package at.ac.fhtw.swen3.swen3teamm.persistance.repository;

import at.ac.fhtw.swen3.swen3teamm.persistance.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {
    //JpaRepository stellt CRUD-Methoden zur Verfügung (save, findById, findAll, deleteById, ...)
    /* so wird es verwendet:
    documentRepository.save(doc);               // Speichert ein Document
    documentRepository.findById(uuid);          // Holt ein Document aus der DB
    documentRepository.findAll();               // Holt alle Documents
    documentRepository.deleteById(uuid);        // Löscht ein Document
    */
}
