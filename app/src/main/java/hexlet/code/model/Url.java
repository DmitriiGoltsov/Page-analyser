package hexlet.code.model;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Entity
public final class Url extends Model {

    @Id
    private Long id;
    private String name;
    @WhenCreated
    private Instant createdAt;
    @OneToMany(cascade = CascadeType.ALL)
    private List<UrlCheck> urlChecks;

    public Url(String name) {
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public List<UrlCheck> getUrlChecks() {
        return urlChecks;
    }

    public void addUrlCheck(UrlCheck urlCheck) {
        urlChecks.add(urlCheck);
    }
}
