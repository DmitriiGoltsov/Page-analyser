package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.Instant;

@Getter
@Setter
@ToString
public final class UrlCheck {

    private Long id;

    private int statusCode;

    private String title;

    private String h1;

    private String description;

    private Instant createdAt;

    private Long urlId;


    public UrlCheck(int statusCode, String title, String h1, String description, Instant createdAt) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.createdAt = createdAt;
    }
}
