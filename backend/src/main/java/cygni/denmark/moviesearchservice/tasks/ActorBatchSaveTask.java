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
@RequiredArgsConstructor
public class ActorBatchSaveTask {
    public static final int PG_BATCH_SIZE = 50;
    private final HikariDataSource hikariDataSource;

    public Long batchSave(final List<ActorDb> actors) {
        long toreturn = 0;
        try (Connection connection = hikariDataSource.getConnection()) {
            doBatchSaveActors(actors, connection);
            List<AbstractIngestTask.StringUUID> knownFor = new ArrayList<>();
            List<AbstractIngestTask.StringUUID> primaryProf = new ArrayList<>();

            actors.forEach(s -> {
                s.getKnownForTitles().forEach(known ->
                        knownFor.add(new AbstractIngestTask.StringUUID(s.getId(), known)));
                s.getPrimaryProfession().forEach(primary ->
                        primaryProf.add(new AbstractIngestTask.StringUUID(s.getId(), primary)));
            });

            doBatchSaveKnownFor(knownFor, connection);
            doBatchSavePrimaryProfession(primaryProf, connection);
            toreturn = actors.size();
        } catch (SQLException exception) {

        }
        return toreturn;
    }


    private void doBatchSavePrimaryProfession(List<AbstractIngestTask.StringUUID> knownFor, Connection connection) {
        String sql =
                "INSERT INTO primary_profession (actor_db_id, primary_profession) " +
                        "VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            int counter = 0;
            for (AbstractIngestTask.StringUUID known : knownFor) {
                statement.clearParameters();
                statement.setObject(1, known.getId());
                statement.setString(2, known.getString());
                statement.addBatch();
                if ((counter + 1) % PG_BATCH_SIZE == 0 || (counter + 1) == knownFor.size()) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
                counter++;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doBatchSaveKnownFor(List<AbstractIngestTask.StringUUID> knownFor, Connection connection) {
        String sql =
                "INSERT INTO known_for_titles (actor_db_id, known_for_titles) " +
                        "VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            int counter = 0;
            for (AbstractIngestTask.StringUUID known : knownFor) {
                statement.clearParameters();
                statement.setObject(1, known.getId());
                statement.setString(2, known.getString());

                statement.addBatch();
                if ((counter + 1) % PG_BATCH_SIZE == 0 || (counter + 1) == knownFor.size()) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
                counter++;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

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
