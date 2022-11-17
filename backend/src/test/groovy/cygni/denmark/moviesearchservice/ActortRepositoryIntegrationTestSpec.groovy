package cygni.denmark.moviesearchservice

import cygni.denmark.moviesearchservice.persistence.repositories.ActorDb
import cygni.denmark.moviesearchservice.persistence.repositories.ActorRepository
import cygni.denmark.moviesearchservice.tasks.ActorDbIngestTask
import org.springframework.beans.factory.annotation.Autowired

class ActortRepositoryIntegrationTestSpec extends IntegrationTestSupport {

    @Autowired
    private ActorRepository actorRepository

    @Autowired
    private ActorDbIngestTask actorIngestTask

    void "test that saving in postgres works as expected"() {
        given:
        ActorDb actorDb = new ActorDb(id: UUID.randomUUID())

        when:
        actorRepository.save(actorDb)

        then:
        actorRepository.count() == 1
        cleanup:
        actorRepository.deleteAll()
    }


}
