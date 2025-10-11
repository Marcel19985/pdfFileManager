package at.ac.fhtw.swen3.swen3teamm.service;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(String id) { super("Document not found: " + id); }
}
