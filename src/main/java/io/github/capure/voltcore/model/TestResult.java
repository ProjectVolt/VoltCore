package io.github.capure.voltcore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TestResult {
    @Id
    private Long id;
    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String output;
    @NotNull
    private Integer cpuTime;
    @NotNull
    private Integer realTime;
    @NotNull
    private Integer memory;
    @NotNull
    private Integer signal;
    @NotNull
    private Integer exitCode;
    @NotNull
    private String error;
    @NotNull
    private String result;
}
