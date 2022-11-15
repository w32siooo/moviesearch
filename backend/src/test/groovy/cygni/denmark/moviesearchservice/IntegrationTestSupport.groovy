package cygni.denmark.moviesearchservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.elasticsearch.ElasticsearchContainer
import spock.lang.Shared
import spock.lang.Specification

@ContextConfiguration
@ActiveProfiles(["test"])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("GroovyAccessibility")
abstract class IntegrationTestSupport extends Specification {

    private static final String DOCKER_ELASTIC_IMG = "docker.elastic.co/elasticsearch/elasticsearch:7.17.6";
    private static final String CLUSTER_NAME = "test-cluster";

    private static final String ELASTIC_SEARCH = "elasticsearch";

    @Shared
    private static ElasticsearchContainer elastic = new ElasticsearchContainer(DOCKER_ELASTIC_IMG)

    void setupSpec() {
        elastic.addFixedExposedPort(15235, 9200);
        elastic.addEnv(CLUSTER_NAME, ELASTIC_SEARCH);
        elastic.start()
    }

    void cleanupSpec() {
        elastic.stop()
    }
}
