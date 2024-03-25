package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    public List<Tag> findAllByNameLikeIgnoreCase(String name, Pageable pageable);
}
