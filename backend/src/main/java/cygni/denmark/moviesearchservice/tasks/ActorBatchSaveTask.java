package cygni.denmark.moviesearchservice.tasks;

import com.zaxxer.hikari.HikariDataSource;
import cygni.denmark.moviesearchservice.persistence.repositories.ActorDb;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ActorBatchSaveTask extends AbstractBatchSaveTask {
    public ActorBatchSaveTask(HikariDataSource hikariDataSource) {
        super(hikariDataSource);
    }
    private static final String PRIM_PROF_SQL = """
            INSERT INTO primary_profession (actor_db_id, primary_profession) VALUES (?, ?)
                """;
    private static final String KNOWN_FOR_SQL =
            """
INSERT INTO known_for_titles (actor_db_id, known_for_titles)VALUES (?, ?)
""";

    /**
     * This method takes in a list of actors and then batch saves it in the relevant repositories.
     */

    public Long batchSave(final List<ActorDb> actors) {
        long toreturn = 0;
        try (Connection connection = hikariDataSource.getConnection()) {

            doBatchSaveActors(actors, connection);

            List<AbstractIngestTask.StringUUID> knownFor = new ArrayList<>();
            List<AbstractIngestTask.StringUUID> primaryProf = new ArrayList<>();

            flattenInnerListsandEnrich(actors, knownFor, primaryProf);

            batchSaveStringUUID(knownFor, connection, KNOWN_FOR_SQL);
            batchSaveStringUUID(primaryProf, connection, PRIM_PROF_SQL);
            toreturn = actors.size();
        } catch (SQLException ignored) {

        }
        return toreturn;
    }

    private void flattenInnerListsandEnrich(List<ActorDb> actors, List<AbstractIngestTask.StringUUID> knownFor, List<AbstractIngestTask.StringUUID> primaryProf) {
        actors.forEach(actor -> {
            actor.getKnownForTitles().forEach(known ->
                    knownFor.add(new AbstractIngestTask.StringUUID(actor.getId(), known)));
            actor.getPrimaryProfession().forEach(primary ->
                    primaryProf.add(new AbstractIngestTask.StringUUID(actor.getId(), primary)));
        });
    }

    private void doBatchSaveActors(List<ActorDb> actorData, Connection connection) {
        String sql =
                "INSERT INTO actors (id, version, timestamp,nconst, birth_year, death_year, primary_name) " +
                        "VALUES (?, ?, ?, ?, ?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            int counter = 0;
            for (ActorDb actor : actorData) {
                statement.clearParameters();
                statement.setObject(1, actor.getId());
                statement.setLong(2, actor.getVersion());
                statement.setTimestamp(3,actor.getTimestamp());
                statement.setString(4, actor.getNconst());
                statement.setInt(5, actor.getBirthYear());
                statement.setInt(6, actor.getDeathYear());
                statement.setString(7, actor.getPrimaryName());
                statement.addBatch();
                if ((counter + 1) % PG_BATCH_SIZE == 0 || (counter + 1) == actorData.size()) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
