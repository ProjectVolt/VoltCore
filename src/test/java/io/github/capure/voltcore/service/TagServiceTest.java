package io.github.capure.voltcore.service;

import io.github.capure.voltcore.dto.GetTagDto;
import io.github.capure.voltcore.exception.FailedCreateException;
import io.github.capure.voltcore.model.Tag;
import io.github.capure.voltcore.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TagService.class})
@EnableMethodSecurity
public class TagServiceTest {
    @Autowired
    private TagService tagService;

    @MockBean
    private TagRepository tagRepository;

    @MockBean
    private UserService userService;

    @WithMockUser(roles = {"USER"})
    @Test
    public void createShouldThrowForInvalidUserRole() {
        assertThrows(AccessDeniedException.class, () -> tagService.create("temp"));
    }

    @WithMockUser(roles = {"STAFF"})
    @Test
    public void createShouldWorkForStaffUserRole() {
        when(tagRepository.save(any())).thenReturn(new Tag(1L, "temp", Set.of()));
        assertDoesNotThrow(() -> tagService.create("temp"));
    }

    @WithMockUser(roles = {"ADMIN"})
    @Test
    public void createShouldWorkForAdminUserRole() {
        when(tagRepository.save(any())).thenReturn(new Tag(1L, "temp", Set.of()));
        assertDoesNotThrow(() -> tagService.create("temp"));
    }

    @WithMockUser(roles = {"STAFF"})
    @Test
    public void createShouldReturnTagWithCorrectData() {
        String name = "temp";
        when(tagRepository.save(any())).thenReturn(new Tag(1L, name, Set.of()));

        GetTagDto result = assertDoesNotThrow(() -> tagService.create(name));

        assertEquals(name, result.getName());
    }

    @WithMockUser(roles = {"STAFF"})
    @Test
    public void createShouldReturnTagWithCorrectDataEvenIfTagAlreadyExists() {
        String name = "temp";
        when(tagRepository.save(any())).thenThrow(DataIntegrityViolationException.class);
        when(tagRepository.findByName(name)).thenReturn(Optional.of(new Tag(1L, name, Set.of())));

        GetTagDto result = assertDoesNotThrow(() -> tagService.create(name));

        assertEquals(name, result.getName());
    }

    @WithMockUser(roles = {"STAFF"})
    @Test
    public void createShouldHandleUnexpectedDbState() {
        String name = "temp";
        when(tagRepository.save(any())).thenThrow(DataIntegrityViolationException.class);
        when(tagRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(FailedCreateException.class, () -> tagService.create(name));
    }
}
