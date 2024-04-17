package io.github.capure.voltcore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @NotNull
    private boolean visible;
    @NotNull
    private String name;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @NotNull
    private String languages;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String template;
    @ManyToOne
    @JoinColumn(name = "added_by_id", nullable = false)
    private User addedBy;
    @NotNull
    private int timeLimit;
    @NotNull
    private int memoryLimit;
    @NotNull
    private String difficulty;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Tag> tags;
    private String author;
    @OneToMany(mappedBy = "problem")
    private Set<TestCase> testCases;
    @OneToMany(mappedBy = "problem")
    private Set<Submission> submissions;
    @NotNull
    private int transparentTestCases;
    @NotNull
    private int totalScore;
    @NotNull
    private int submissionCount;
    @NotNull
    private int acceptedSubmissions;
    @NotNull
    private int wrongSubmissions;
    @NotNull
    private int partiallyAccepted;
    @NotNull
    private int runtimeErrors;
    @NotNull
    private int compileErrors;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Problem problem = (Problem) o;
        return getId() != null && Objects.equals(getId(), problem.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
