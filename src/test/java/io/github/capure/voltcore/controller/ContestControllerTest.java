package io.github.capure.voltcore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.capure.voltcore.config.SecurityConfig;
import io.github.capure.voltcore.dto.*;
import io.github.capure.voltcore.dto.admin.AdminGetProblemDto;
import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.service.ContestService;
import io.github.capure.voltcore.service.ProblemService;
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

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ContestController.class, JwtUtil.class, UserDetailsServiceImpl.class})
public class ContestControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private ContestController contestController;

    @MockBean
    private ContestService contestService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CreateContestDto getData() {
        return new CreateContestDto("Test contest", "description", null, 1L, 2000000000L, true);
    }

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(contestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldReturnGetContestDtoForValidData() throws Exception {
        CreateContestDto data = getData();
        GetContestDto res = new GetContestDto();
        res.setName(data.getName());
        when(contestService.create(any(), any())).thenReturn(res);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/contest/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name", is(data.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldReturn400ForInvalidData() throws Exception {
        CreateContestDto data = getData();
        data.setName("*A#");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/contest/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldReturn500ForDbError() throws Exception {
        CreateContestDto data = getData();
        when(contestService.create(any(), any())).thenThrow(RuntimeException.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/contest/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldReturnGetContestDtoForValidData() throws Exception {
        PutContestDto data = new PutContestDto();
        GetContestDto getContestDto = new GetContestDto();
        getContestDto.setName(data.getName());
        when(contestService.edit(any(), eq(1L), any())).thenReturn(getContestDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/contest/1")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name", is(data.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldReturn400ForInvalidData() throws Exception {
        PutContestDto data = new PutContestDto();
        data.setName("*A#");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/contest/1")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldReturn400ForInvalidId() throws Exception {
        PutContestDto data = new PutContestDto();
        when(contestService.edit(any(), eq(1L), any())).thenThrow(InvalidIdException.class);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/api/contest/1")
                .content(asJsonString(data))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldReturn500ForDbError() throws Exception {
        PutContestDto data = new PutContestDto();
        when(contestService.edit(any(), any(), any())).thenThrow(RuntimeException.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/contest/1")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllShouldSend200AndReturnListOfContestsForValidParams() throws Exception {
        CreateContestDto data = getData();
        GetContestDto getContestDto = new GetContestDto();
        getContestDto.setName(data.getName());
        Mockito.when(contestService.getAll(any(), any(), anyInt(), anyInt())).thenReturn(List.of(getContestDto));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/contest/")
                        .param("search", "contest")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name", is(data.getName())));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/contest/")
                        .param("visible", "false")
                        .param("search", "contest")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name", is(data.getName())));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/contest/")
                        .param("visible", "true")
                        .param("search", "contest")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name", is(data.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllShouldSend400ForInvalidOrMissingParams() throws Exception {
        CreateContestDto data = getData();
        GetContestDto getContestDto = new GetContestDto();
        getContestDto.setName(data.getName());
        Mockito.when(contestService.getAll(any(), any(), anyInt(), anyInt())).thenReturn(List.of(getContestDto));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/contest/")
                        .param("visible", "123")
                        .param("search", "contest")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/contest/")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/contest/")
                        .param("search", "contest")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/contest/")
                        .param("search", "contest")
                        .param("page", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/contest/")
                        .param("search", "contest")
                        .param("page", "0")
                        .param("pageSize", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getShouldReturnGetContestDtoForValidData() throws Exception {
        GetContestDto getContestDto = new GetContestDto();
        getContestDto.setName("hello");
        when(contestService.get(any(), any(), any())).thenReturn(getContestDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/contest/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name", is(getContestDto.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getShouldReturn400ForInvalidId() throws Exception {
        when(contestService.get(any(), any(), any())).thenThrow(InvalidIdException.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/contest/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getWithPasswordShouldReturnGetContestDtoForValidData() throws Exception {
        GetContestDto getContestDto = new GetContestDto();
        getContestDto.setName("hello");
        when(contestService.get(any(), any(), any())).thenAnswer(a -> {
            assertEquals("pass", a.getArgument(2));
            return getContestDto;
        });

        GetContestPasswordDto data = new GetContestPasswordDto("pass");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/contest/1")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name", is(getContestDto.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getWithPasswordShouldReturn400ForInvalidPasswordFormat() throws Exception {
        GetContestDto getContestDto = new GetContestDto();
        getContestDto.setName("hello");
        when(contestService.get(any(), any(), any())).thenAnswer(a -> getContestDto);

        GetContestPasswordDto data = new GetContestPasswordDto(null);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/contest/1")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        GetContestPasswordDto data1 = new GetContestPasswordDto("");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/contest/1")
                        .content(asJsonString(data1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getWithPasswordShouldReturn400ForInvalidId() throws Exception {
        when(contestService.get(any(), any(), any())).thenThrow(InvalidIdException.class);
        GetContestPasswordDto data = new GetContestPasswordDto("pass");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/contest/1")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
