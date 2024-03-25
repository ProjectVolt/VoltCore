package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    public Optional<Tag> findByName(String name);

    public List<Tag> findAllByNameLikeIgnoreCase(String name, Pageable pageable);
}
