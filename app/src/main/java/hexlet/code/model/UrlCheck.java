package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.sql.Timestamp;
import java.time.Instant;

@ToString
public final class UrlCheck {

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private int statusCode;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String h1;

    @Getter
    @Setter
    private String description;

    @Setter
    private Timestamp createdAt;

    @Getter
    @Setter
    private Long urlId;

    public UrlCheck(int statusCode, String title, String h1, String description, Timestamp createdAt, Long urlId) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.createdAt = createdAt;
        this.urlId = urlId;
    }

    public UrlCheck(int statusCode, String title, String h1, String description) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt.toInstant();
    }
}
