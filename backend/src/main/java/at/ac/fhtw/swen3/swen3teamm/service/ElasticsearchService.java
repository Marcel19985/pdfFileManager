package at.ac.fhtw.swen3.swen3teamm.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final ElasticsearchClient esClient;

    public List<String> searchDocumentsByText(String queryText) throws IOException {

        SearchResponse<Object> response = esClient.search(
                s -> s.index("documents")
                        .query(q -> q.match(m -> m.field("text").query(queryText))),
                Object.class
        );

        return response.hits().hits().stream()
                .map(Hit::id)
                .toList();
    }

    public void deleteDocument(String documentId) throws IOException {
        DeleteResponse response = esClient.delete(d -> d
                .index("documents")
                .id(documentId)
        );

        if (!response.result().toString().equalsIgnoreCase("deleted")) {
            // optional: Log, falls nicht gelöscht
            System.out.println("Document " + documentId + " not deleted in Elasticsearch. Result: " + response.result());
        }
    }
}
