package hexlet.code.repository;

import hexlet.code.model.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlRepository.class.getName());

    public static void save(Url url) {
        String query = "INSERT INTO urls (name, created_at) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, url.getName());
            preparedStatement.setTimestamp(2, url.getCreatedAt());

            System.out.println(preparedStatement);

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                url.setId(generatedKeys.getLong("id"));
            }
        } catch (SQLException throwables) {
            System.out.println(throwables.getErrorCode());
            System.out.println(throwables.getSQLState());
            System.out.println(throwables.getMessage());
            throw new RuntimeException("DB has not returned an id after attempt to save the entity!");
        }
    }

    public static Optional<Url> findByName(String name) throws SQLException {
        String query = "SELECT * FROM urls WHERE name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(query)) {

            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Timestamp createdAd = resultSet.getTimestamp("created_at");
                Url url = new Url(name, createdAd);
                url.setId(id);
                return Optional.of(url);
            }

            return Optional.empty();
        } catch (SQLException throwables) {
            throw new SQLException("Url with name " + name + " was now found");
        }
    }

    public static Optional<Url> findById(Long id) throws SQLException {
        String query = "SELECT * FROM urls WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(query)) {

            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("name");
                Timestamp createdAd = resultSet.getTimestamp("created_at");
                Url url = new Url(name, createdAd);
                url.setId(id);
                return Optional.of(url);
            }
            return Optional.empty();
        } catch (SQLException throwables) {
            throw new SQLException("Url with id " + id + " was now found");
        }
    }

    public static List<Url> getUrls() throws SQLException {
        String query = "SELECT * FROM urls";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            List<Url> urls = new ArrayList<>();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                Timestamp createdAd = resultSet.getTimestamp("created_at");
                Url url = new Url(name, createdAd);
                url.setId(id);

                urls.add(url);
            }

            return urls;
        } catch (SQLException throwables) {
            throw new SQLException("The entities were not found in DB!");
        }
    }

    public static void truncateDB() throws SQLException {
        String query = "TRUNCATE TABLE urls RESTART IDENTITY";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(query)) {

            preparedStatement.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new SQLException("Truncate task on table url has failed!");
        }
    }

    public static boolean delete(Long id) throws SQLException {
        String query = "DELETE FROM urls WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(query)) {

            preparedStatement.setLong(1, id);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new SQLException("The url entity with id " + id + "was not deleted");
        }
    }
}
