package cygni.denmark.moviesearchservice.tasks;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractBatchSaveTask {
  protected final HikariDataSource hikariDataSource;

  public static final int PG_BATCH_SIZE = 50;

  protected void batchSaveStringUUID(
      List<AbstractIngestTask.StringUUID> knownFor, Connection connection, String sql) {
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
}
