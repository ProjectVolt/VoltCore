package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Transactional
public class TagRepositoryTest {
    @Autowired
    private TagRepository tagRepository;

    @Test
    @DirtiesContext
    public void findAllByUsernameLikeIgnoreCaseWorks() {
        for (int i = 0; i < 100; i++) {
            Tag tag = new Tag();
            tag.setName(String.format("tag%02d", i));
            tagRepository.save(tag);
        }
        List<Tag> results = assertDoesNotThrow(() -> tagRepository.findAllByNameLikeIgnoreCase("%tag%", PageRequest.of(0, 10)));
        assertEquals(10, results.size());
        assertEquals("tag09", results.getLast().getName());
        results = assertDoesNotThrow(() -> tagRepository.findAllByNameLikeIgnoreCase("%tag%", PageRequest.of(1, 10)));
        assertEquals(10, results.size());
        assertEquals("tag19", results.getLast().getName());
    }
}
