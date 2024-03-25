package io.github.capure.voltcore.service;

import io.github.capure.voltcore.dto.GetTagDto;
import io.github.capure.voltcore.exception.FailedCreateException;
import io.github.capure.voltcore.model.Tag;
import io.github.capure.voltcore.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    public GetTagDto create(String name) throws FailedCreateException {
        try {
            log.info("Adding new tag - {}", name);
            Tag result = tagRepository.save(new Tag(null, name));
            log.info("Added tag successfully");
            return new GetTagDto(result);
        } catch (DataIntegrityViolationException ex) {
            log.info("Failed adding new tag, because it already exists");
            Tag result = tagRepository.findByName(name).orElseThrow(() -> {
                log.error("Couldn't find the tag which was expected to exist");
                return new FailedCreateException();
            });
            log.info("Returning tag from database");
            return new GetTagDto(result);
        }
    }

    public List<GetTagDto> getAll(String search, int pageNumber, int pageSize) {
        List<Tag> tags = tagRepository.findAllByNameLikeIgnoreCase(search, PageRequest.of(pageNumber, pageSize));
        return tags.parallelStream().map(GetTagDto::new).toList();
    }
}
