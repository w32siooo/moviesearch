package cygni.denmark.moviesearchservice

import cygni.denmark.moviesearchservice.search.documents.ActorDocument
import cygni.denmark.moviesearchservice.search.repositories.ActorDocumentRepository
import org.springframework.beans.factory.annotation.Autowired


class ActorDocumentRepositoryIntegrationSpec extends IntegrationTestSupport {

    @Autowired
    private ActorDocumentRepository searchRepository

    void "test search repo"() {
        given: "Document is created"
        ActorDocument actorDoc = new ActorDocument("1", 0L, "se",
                "mo", 1123, 1234, ["123", "sdafj"], ["123", "swfsf"])

        when: "Entity is saved"
        ActorDocument response = searchRepository.save(actorDoc).block()

        and: "Code is executed"
        def actorDocRes = searchRepository.findById("1").block()

        then: "verify that response is correct"
        response == actorDocRes

    }
}
