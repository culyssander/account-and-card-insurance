package com.santander.msnotificationservices.config;

import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import org.elasticsearch.client.RestClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

import org.apache.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(
                        new HttpHost("localhost", 9200, "http"))
                .build();

        ElasticsearchTransport transport =
                new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }

    @Bean
    public BulkIngester<Void> bulkIngester(ElasticsearchClient client) {
        return BulkIngester.of(b -> b
                .client(client)
                .maxOperations(500)
                .flushInterval(2, TimeUnit.SECONDS)
        );
    }
}
