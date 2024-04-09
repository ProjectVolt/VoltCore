package io.github.capure.voltcore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;
    @CreationTimestamp
    private Instant createdOn;
    @ManyToOne
    @JoinColumn(name = "added_by_id", nullable = false)
    private User addedBy;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sourceCode;
    @NotNull
    private String language;
    @NotNull
    private String status;
    @NotNull
    private Boolean compileSuccess;
    @NotNull
    private Boolean runSuccess;
    @NotNull
    private Boolean answerSuccess;
    private String compileErrorMessage;
    private Boolean compileErrorFatal;
    @OneToMany(mappedBy = "submission")
    private List<TestResult> testResults;
    @NotNull
    private Integer maxCpu;
    @NotNull
    private Integer maxMemory;
    @NotNull
    private Integer score;
}
