package cygni.denmark.moviesearchservice


import cygni.denmark.moviesearchservice.search.repositories.ActorDocumentRepository
import cygni.denmark.moviesearchservice.tasks.ActorBatchSaveTask
import cygni.denmark.moviesearchservice.tasks.ActorDbIngestTask
import spock.lang.Specification

class ActorDbIngestTaskSpec extends Specification {

    ActorDbIngestTask actorIngestTask
    ActorDocumentRepository actorDocumentRepository

    ActorBatchSaveTask actorBatchSaveTask

    void setup() {
        actorDocumentRepository = Mock(ActorDocumentRepository)
        actorBatchSaveTask = Mock(ActorBatchSaveTask)
        actorIngestTask = new ActorDbIngestTask(actorBatchSaveTask)
        actorIngestTask.take = 500
        actorIngestTask.PG_BUFFER_SIZE = 50
        actorIngestTask.NAME_BASICS_TSV_PATH = getClass().getClassLoader().getResource("testdata/namebasics.tgv").path
    }


    void "test actor ingest"() {

        when:
        def res = actorIngestTask.run().block()

        then:
        1 * actorBatchSaveTask.batchSave(_) >> 1
        res == 1

    }


}
