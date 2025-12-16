package at.ac.fhtw.swen3.swen3teamm.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchService {

    private final ElasticsearchClient esClient;

    public List<String> searchDocumentsByText(String queryText) throws IOException {
        log.info("Searching documents in Elasticsearch with query text: '{}'", queryText);
        SearchResponse<Object> response = esClient.search(
                s -> s.index("documents")
                        .query(q -> q.match(m -> m.field("text").query(queryText))),
                Object.class
        );

        log.info("Elasticsearch search finished.");

        return response.hits().hits().stream()
                .map(Hit::id)
                .toList();
    }

    public void deleteDocument(String documentId) throws IOException {
        log.info("Deleting document with id '{}' from Elasticsearch", documentId);
        DeleteResponse response = esClient.delete(d -> d
                .index("documents")
                .id(documentId)
        );

        if (!response.result().toString().equalsIgnoreCase("deleted")) {
            // optional: Log, falls nicht gelöscht
            log.warn("Document {} not deleted in Elasticsearch. Result: {}", documentId, response.result());
        }
    }
}
