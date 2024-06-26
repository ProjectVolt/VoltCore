package io.github.capure.voltcore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.capure.voltcore.config.SecurityConfig;
import io.github.capure.voltcore.dto.CreateSubmissionDto;
import io.github.capure.voltcore.dto.GetSubmissionDto;
import io.github.capure.voltcore.exception.ContestClosedException;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.model.Submission;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.service.SubmissionService;
import io.github.capure.voltcore.service.UserDetailsServiceImpl;
import io.github.capure.voltcore.util.GlobalExceptionHandler;
import io.github.capure.voltcore.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SubmissionController.class, JwtUtil.class, UserDetailsServiceImpl.class})
public class SubmissionControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private SubmissionController submissionController;

    @MockBean
    private SubmissionService submissionService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CreateSubmissionDto getData() {
        return new CreateSubmissionDto(1L, "source", "python");
    }

    private User getUser() {
        return new User(1L,
                "tester",
                "password1",
                "tester@example.com",
                true,
                "ROLE_USER",
                "https://example.com",
                "https://github.com/Capure",
                null,
                0,
                0,
                0,
                Set.of(),
                Set.of(),
                Set.of());
    }

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(submissionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @WithMockUser
    public void createShouldReturnGetSubmissionDtoForValidData() throws Exception {
        CreateSubmissionDto data = getData();
        Submission submission = new Submission();
        submission.setId(1L);
        Problem problem = new Problem();
        problem.setId(1L);
        submission.setProblem(problem);
        submission.setAddedBy(getUser());
        submission.setTestResults(List.of());
        when(submissionService.create(any(), any())).thenReturn(new GetSubmissionDto(submission, false));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/submission/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.problemId", is(data.getProblemId().intValue())));
    }

    @Test
    @WithMockUser
    public void createShouldReturn400ForInvalidData() throws Exception {
        CreateSubmissionDto data = getData();
        data.setLanguage("C#");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/submission/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void createShouldReturn403ForContestClosed() throws Exception {
        CreateSubmissionDto data = getData();
        when(submissionService.create(any(), any())).thenThrow(ContestClosedException.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/submission/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void createShouldReturn500ForRuntimeError() throws Exception {
        CreateSubmissionDto data = getData();
        when(submissionService.create(any(), any())).thenThrow(RuntimeException.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/submission/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    public void getShouldReturnGetSubmissionDtoForValidData() throws Exception {
        Submission submission = new Submission();
        submission.setId(1L);
        Problem problem = new Problem();
        problem.setId(1L);
        submission.setProblem(problem);
        submission.setAddedBy(getUser());
        submission.setTestResults(List.of());
        when(submissionService.get(any(), any())).thenReturn(new GetSubmissionDto(submission, false));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.problemId", is(submission.getProblem().getId().intValue())));
    }

    @Test
    @WithMockUser
    public void getShouldReturn400ForInvalidIdFormat() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void getByUserAndProblemIdShouldReturnGetSubmissionDtoListForValidData() throws Exception {
        Submission submission = new Submission();
        submission.setId(1L);
        Problem problem = new Problem();
        problem.setId(1L);
        submission.setProblem(problem);
        submission.setAddedBy(getUser());
        submission.setTestResults(List.of());
        when(submissionService.getByUserAndProblemId(any(), any(), any())).thenReturn(List.of(new GetSubmissionDto(submission, false)));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/problem/1")
                        .param("limit", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].problemId", is(submission.getProblem().getId().intValue())));
    }

    @Test
    @WithMockUser
    public void getByUserAndProblemIdShouldReturn400ForInvalidIdFormat() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/problem/abc")
                        .param("limit", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/problem/0")
                        .param("limit", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void getByUserAndProblemIdShouldReturn400ForMissingLimitParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/problem/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllShouldSend200AndReturnListOfSubmissionsForValidParams() throws Exception {
        GetSubmissionDto data = new GetSubmissionDto();
        data.setId(7L);
        Mockito.when(submissionService.getAll(any(), any(), any())).thenAnswer(a -> {
            assertNull(a.getArgument(0));
            return List.of(data);
        });

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id", is(data.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllShouldHandleContestId() throws Exception {
        GetSubmissionDto data = new GetSubmissionDto();
        data.setId(7L);
        Mockito.when(submissionService.getAll(any(), any(), any())).thenAnswer(a -> {
            assertEquals(1L, (Long) a.getArgument(0));
            return List.of(data);
        });

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/")
                        .param("contestId", "1")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id", is(data.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllShouldSend400ForInvalidOrMissingParams() throws Exception {
        GetSubmissionDto data = new GetSubmissionDto();
        data.setId(7L);
        Mockito.when(submissionService.getAll(any(), any(), any())).thenReturn(List.of(data));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/")
                        .param("page", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/")
                        .param("page", "-1")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/submission/")
                        .param("page", "0")
                        .param("pageSize", "51"))
                .andExpect(status().isBadRequest());
    }
}
