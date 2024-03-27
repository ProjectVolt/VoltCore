package io.github.capure.voltcore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;
    @Column(nullable = false)
    private String name;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String output;
    @Column(nullable = false)
    private int maxScore;
}