package at.ac.fhtw.swen3.swen3teamm.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${ELASTIC_HOST:http://localhost:9200}")
    private String elasticHost;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // Host aus Env-Var nutzen
        RestClient restClient = RestClient.builder(
                HttpHost.create(elasticHost)
        ).build();

        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}
