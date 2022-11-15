package cygni.denmark.moviesearchservice.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;

@ComponentScan
@Configuration
public class MovieSearchAutoConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticHost;

    @Bean
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(elasticHost)
                .build();
        return ReactiveRestClients.create(clientConfiguration);
    }


}
