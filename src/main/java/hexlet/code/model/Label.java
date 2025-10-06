package hexlet.code.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Id;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "labels")
@Getter
@Setter
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Size(min = 3, max = 1000, message = "Name must be between 3 and 1000 characters")
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @ManyToMany(mappedBy = "labels")
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();

    public Label() {
        this.createdAt = LocalDate.now();
    }

    public Label(String name) {
        this();
        this.name = name;
    }
}
