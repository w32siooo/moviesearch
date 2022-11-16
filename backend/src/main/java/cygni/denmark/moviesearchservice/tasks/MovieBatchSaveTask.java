package cygni.denmark.moviesearchservice.tasks;

import com.zaxxer.hikari.HikariDataSource;
import cygni.denmark.moviesearchservice.persistence.repositories.MovieDb;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MovieBatchSaveTask extends AbstractBatchSaveTask {
  private static final String GENRES_SQL =
      "INSERT INTO genres (movie_db_id, genres) " + "VALUES (?, ?)";

  public MovieBatchSaveTask(HikariDataSource hikariDataSource) {
    super(hikariDataSource);
  }

  public Long batchSave(final List<MovieDb> movies) {
    long toreturn = 0;
    try (Connection connection = hikariDataSource.getConnection()) {

      doBatchSaveMovies(movies, connection);

      List<AbstractIngestTask.StringUUID> genres = new ArrayList<>();

      flattenInnerListsandEnrich(movies, genres);
      batchSaveStringUUID(genres, connection, GENRES_SQL);

      toreturn = movies.size();
    } catch (SQLException ignored) {

    }
    return toreturn;
  }

  public void doBatchSaveMovies(List<MovieDb> movieData, Connection connection) {
    String sql =
        "INSERT INTO movies (id, end_year,original_title,primary_title,"
            + "runtime_minutes,start_year,tconst,title_type,version) "
            + "VALUES (?, ?, ?, ?, ?,?,?,?,?)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      int counter = 0;
      for (MovieDb movie : movieData) {
        statement.clearParameters();
        bindToPreparedStatement(statement, movie);
        statement.addBatch();
        if ((counter + 1) % PG_BATCH_SIZE == 0 || (counter + 1) == movieData.size()) {
          statement.executeBatch();
          statement.clearBatch();
        }
        counter++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void bindToPreparedStatement(PreparedStatement statement, MovieDb movie) throws SQLException {
    statement.setObject(1, movie.getId());
    statement.setInt(2, movie.getEndYear());
    statement.setString(
        3,
        movie.getOriginalTitle().length() > 30
            ? movie.getOriginalTitle().substring(0, 30)
            : movie.getOriginalTitle());
    statement.setString(4, movie.getPrimaryTitle().length() > 30
            ? movie.getPrimaryTitle().substring(0, 30)
            : movie.getPrimaryTitle());
    statement.setInt(5, movie.getRuntimeMinutes());
    statement.setInt(6, movie.getStartYear());
    statement.setString(7, movie.getTconst());
    statement.setString(8, movie.getTitleType());
    statement.setLong(9, movie.getVersion());
  }

  private void flattenInnerListsandEnrich(
      List<MovieDb> movies, List<AbstractIngestTask.StringUUID> genres) {
    movies.forEach(
        mov -> {
          mov.getGenres()
              .forEach(
                  primary -> genres.add(new AbstractIngestTask.StringUUID(mov.getId(), primary)));
        });
  }
}
