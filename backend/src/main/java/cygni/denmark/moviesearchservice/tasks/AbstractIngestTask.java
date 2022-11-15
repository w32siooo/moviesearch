package cygni.denmark.moviesearchservice.tasks;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.BaseStream;

public abstract class AbstractIngestTask {
    @Value("${cygni.take}")
    public Integer take;

    protected Flux<String> getLines(String path) {
        return Flux.using(
                () -> Files.lines(Path.of(path), StandardCharsets.UTF_8),
                Flux::fromStream,
                BaseStream::close
        );
    }

    @Getter
    protected static class StringUUID {
        private final UUID id;
        private final String string;

        public StringUUID(UUID id, String known) {
            this.id = id;
            this.string = known;
        }
    }

    protected Mono<UUID> reactiveUUIDGenerator() {
        return Mono.fromCallable(UUID::randomUUID).subscribeOn(Schedulers.boundedElastic());
    }
}
