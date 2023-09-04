package hexlet.code.repository;

import hexlet.code.model.UrlCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class UrlCheckRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlCheckRepository.class.getName());

    public static void save(UrlCheck urlCheck) {

        LOGGER.info("UrlCheckRepository's method save() was started!");

        String query = """
                        INSERT INTO url_checks (status_code, title, h1, description, created_at, url_id)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, urlCheck.getStatusCode());
            preparedStatement.setString(2, urlCheck.getTitle());
            preparedStatement.setString(3, urlCheck.getH1());
            preparedStatement.setString(4, urlCheck.getDescription());
            preparedStatement.setTimestamp(5, Timestamp.from(urlCheck.getCreatedAt()));
            preparedStatement.setLong(6, urlCheck.getUrlId());

            LOGGER.info("preparedStatement is: " + preparedStatement);

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                urlCheck.setId(generatedKeys.getLong("id"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.out.println(throwables.getMessage());
            throw new RuntimeException("DB has not returned an id after attempt to save the UrlCheck entity!");
        }
    }

    public static Optional<UrlCheck> findLastCheckByUrlId(Long urlId) throws SQLException {
        List<UrlCheck> urlChecks = getAllChecks(urlId);

        if (urlChecks.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(urlChecks.get(0));
    }

    public static List<UrlCheck> getAllChecks(Long urlId) throws SQLException {
        String query = "SELECT * FROM url_checks WHERE url_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, urlId);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<UrlCheck> urlChecks = new ArrayList<>();
            while (resultSet.next()) {
                Long urlCheckId = resultSet.getLong("id");
                Integer statusCode = resultSet.getInt("status_code");
                String title = resultSet.getString("title");
                String h1 = resultSet.getString("h1");
                String description = resultSet.getString("description");
                Timestamp createdAt = resultSet.getTimestamp("created_at");

                UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, createdAt, urlId);
                urlCheck.setId(urlCheckId);

                urlChecks.add(urlCheck);

                Comparator<UrlCheck> comparator = Comparator.comparing(UrlCheck::getCreatedAt);

                urlChecks.sort(comparator);
            }
            return urlChecks;
        } catch (SQLException throwables) {
            throw new SQLException("DB does not find checks of url with id " + urlId);
        }
    }

    public static void truncateDB() throws SQLException {
        String query = "TRUNCATE TABLE url_checks RESTART IDENTITY";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(query)) {

            preparedStatement.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new SQLException("Truncate task on table url_checks has failed!");
        }
    }
}