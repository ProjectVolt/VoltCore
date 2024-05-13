package io.github.capure.voltcore.service;

import io.github.capure.voltcore.dto.CreateContestDto;
import io.github.capure.voltcore.dto.GetContestDto;
import io.github.capure.voltcore.dto.PutContestDto;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.model.Contest;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.repository.ContestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ContestService {
    @Autowired
    private ContestRepository contestRepository;

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    @Transactional("transactionManager")
    public GetContestDto create(User user, CreateContestDto data) {
        Contest contest = new Contest();

        log.info("Adding new contest - name: {} addedBy: {} - {}", data.getName(), user.getId(), user.getUsername());
        contest.setName(data.getName());
        contest.setDescription(data.getDescription());
        if (data.getPassword() != null) contest.setPassword(data.getPassword().isEmpty() ? null : data.getPassword());
        contest.setVisible(data.getVisible());
        contest.setAddedBy(user);

        log.info("Setting start and end times");
        contest.setStartTime(Instant.ofEpochSecond(data.getStartTime()));
        contest.setEndTime(Instant.ofEpochSecond(data.getEndTime()));

        Contest saved = contestRepository.save(contest);

        return new GetContestDto(saved, true);
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    @Transactional(value = "transactionManager", rollbackFor = {InvalidIdException.class})
    public GetContestDto edit(User user, Long id, PutContestDto data) throws InvalidIdException {
        Contest contest = contestRepository.findById(id).orElseThrow(InvalidIdException::new);

        log.info("Editing contest - name: {} addedBy: {} - {}", data.getName(), user.getId(), user.getUsername());

        if (user.getRole().equals("ROLE_STAFF")) {
            log.info("Checking user permission");
            if (!Objects.equals(contest.getAddedBy().getId(), user.getId())) {
                log.info("User is not an admin or an owner");
                throw new AccessDeniedException("Not an owner");
            }
        }

        if (data.getVisible() != null) contest.setVisible(data.getVisible());
        if (data.getName() != null) contest.setName(data.getName());
        if (data.getDescription() != null) contest.setDescription(data.getDescription());

        if (data.getPassword() != null) contest.setPassword(data.getPassword().isEmpty() ? null : data.getPassword());
        else contest.setPassword(null);

        if (data.getStartTime() != null) contest.setStartTime(Instant.ofEpochSecond(data.getStartTime()));
        if (data.getEndTime() != null) contest.setEndTime(Instant.ofEpochSecond(data.getEndTime()));

        log.info("Saving edited contest");
        Contest saved = contestRepository.save(contest);
        log.info("Saved successfully");

        return new GetContestDto(saved, true);
    }

    @PreAuthorize("(#visible != null && #visible) || hasRole('ADMIN') || hasRole('STAFF')")
    @PostFilter("filterObject.getVisible() || hasRole('ADMIN') || filterObject.addedBy == authentication.principal.getId()")
    @Transactional("transactionManager")
    public List<GetContestDto> getAll(Boolean visible, String search, int page, int pageSize) {
        if (visible == null) {
            return new ArrayList<>(contestRepository.findAllByNameLikeIgnoreCaseOrderByStartTimeDesc(search, PageRequest.of(page, pageSize)).stream()
                    .map(c -> new GetContestDto(c, false)).toList());
        } else {
            return new ArrayList<>(contestRepository.findAllByVisibleAndNameLikeIgnoreCaseOrderByStartTimeDesc(visible, search, PageRequest.of(page, pageSize)).stream()
                    .map(c -> new GetContestDto(c, false)).toList());
        }
    }

    @PostAuthorize("returnObject.getVisible() || hasRole('ADMIN') || returnObject.addedBy == authentication.principal.getId()")
    @Transactional(value = "transactionManager", rollbackFor = {InvalidIdException.class})
    public GetContestDto get(User user, Long id, String password) throws InvalidIdException {
        Contest contest = contestRepository.findById(id).orElseThrow(InvalidIdException::new);
        boolean unlocked = false;
        if (contest.getPassword() == null) unlocked = true;
        else if (password != null) {
            if (password.equals(contest.getPassword())) {
                unlocked = true;
            } else {
                throw new AccessDeniedException("Invalid password");
            }
        }
        if (user.getRole().equals("ROLE_ADMIN")) unlocked = true;
        if (user.getRole().equals("ROLE_STAFF") && contest.getAddedBy().getId().equals(user.getId())) unlocked = true;
        return new GetContestDto(contest, unlocked);
    }
}
