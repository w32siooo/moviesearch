package cygni.denmark.moviesearchservice

import cygni.denmark.moviesearchservice.search.documents.ActorDocument

class ActorDocFactory {

    static ActorDocument genActorDoc() {
        return new ActorDocument(UUID.randomUUID().toString(), 0L, "se",
                "robert", 1123, 1234, ["123", "sdafj"], ["123", "swfsf"])
    }
}
