package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@ToString
public final class Url {

    private Long id;

    @ToString.Include
    private String name;
    private Instant createdAt;

    public Url(String name, Instant createdAt, List<UrlCheck> urlChecks) {
        this.name = name;
        this.createdAt = createdAt;
    }
}
