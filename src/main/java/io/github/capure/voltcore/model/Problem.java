package io.github.capure.voltcore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @NotNull
    private boolean visible;
    @NotNull
    private String name;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @NotNull
    private String languages;
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
    @ManyToMany
    private Set<Tag> tags;
    private String author;
    @OneToMany(mappedBy = "problem")
    private Set<TestCase> testCases;
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
}
