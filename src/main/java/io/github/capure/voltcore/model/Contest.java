package io.github.capure.voltcore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Contest {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @NotNull
    @NotEmpty
    private String name;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    private String password;
    @NotNull
    private Instant startTime;
    @NotNull
    private Instant endTime;
    @NotNull
    private Boolean visible;
    @ManyToOne
    @JoinColumn(name = "added_by_id", nullable = false)
    private User addedBy;
    @OneToMany(mappedBy = "contest")
    private Set<Problem> problems;
}