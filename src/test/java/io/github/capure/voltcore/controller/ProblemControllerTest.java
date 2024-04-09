package io.github.capure.voltcore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.capure.voltcore.config.SecurityConfig;
import io.github.capure.voltcore.dto.CreateProblemDto;
import io.github.capure.voltcore.dto.GetProblemDto;
import io.github.capure.voltcore.dto.PutProblemDto;
import io.github.capure.voltcore.dto.admin.AdminGetProblemDto;
import io.github.capure.voltcore.dto.admin.CreateTestCaseDto;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.service.ProblemService;
import io.github.capure.voltcore.service.UserDetailsServiceImpl;
import io.github.capure.voltcore.util.JwtUtil;
import jakarta.servlet.ServletException;
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

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ProblemService.class, ProblemController.class, JwtUtil.class, UserDetailsServiceImpl.class})
public class ProblemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProblemService problemService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CreateProblemDto getData() {
        return new CreateProblemDto(false,
                "problem",
                "this is a test",
                List.of("python"),
                null,
                1000,
                1000,
                "easy",
                List.of(),
                null,
                List.of(new CreateTestCaseDto("test", "in", "out", 10)),
                0);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldReturnGetProblemDtoForValidData() throws Exception {
        CreateProblemDto data = getData();
        AdminGetProblemDto getProblemDto = new AdminGetProblemDto();
        getProblemDto.setName(data.getName());
        when(problemService.create(any(), any())).thenReturn(getProblemDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/problem/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name", is(data.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldReturn400ForInvalidData() throws Exception {
        CreateProblemDto data = getData();
        data.setName("*A#");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/problem/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldReturn400ForMissingTestCases() throws Exception {
        CreateProblemDto data = getData();
        data.setTestCases(List.of());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/problem/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldReturn500ForDbError() throws Exception {
        CreateProblemDto data = getData();
        when(problemService.create(any(), any())).thenThrow(RuntimeException.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/problem/")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldReturnGetProblemDtoForValidData() throws Exception {
        PutProblemDto data = new PutProblemDto();
        AdminGetProblemDto getProblemDto = new AdminGetProblemDto();
        getProblemDto.setName(data.getName());
        when(problemService.edit(eq(1L), any())).thenReturn(getProblemDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/problem/1")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name", is(data.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldReturn400ForInvalidData() throws Exception {
        PutProblemDto data = new PutProblemDto();
        data.setName("*A#");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/problem/1")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldReturn400ForInvalidId() throws Exception {
        PutProblemDto data = new PutProblemDto();
        when(problemService.edit(eq(1L), any())).thenThrow(InvalidIdException.class);

        ServletException ex = assertThrows(ServletException.class, () -> mockMvc.perform(MockMvcRequestBuilders
                .put("/api/problem/1")
                .content(asJsonString(data))
                .contentType(MediaType.APPLICATION_JSON)));

        assertEquals(ex.getRootCause().getClass(), InvalidIdException.class);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldReturn500ForDbError() throws Exception {
        PutProblemDto data = new PutProblemDto();
        when(problemService.edit(any(), any())).thenThrow(RuntimeException.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/problem/1")
                        .content(asJsonString(data))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllShouldSend200AndReturnListOfProblemsForValidParams() throws Exception {
        CreateProblemDto data = getData();
        GetProblemDto getProblemDto = new GetProblemDto();
        getProblemDto.setName(data.getName());
        Mockito.when(problemService.getAll(any(), any(), anyInt(), anyInt())).thenReturn(List.of(getProblemDto));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/")
                        .param("search", "problem")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name", is(data.getName())));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/")
                        .param("visible", "false")
                        .param("search", "problem")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name", is(data.getName())));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/")
                        .param("visible", "true")
                        .param("search", "problem")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name", is(data.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllShouldSend400ForInvalidOrMissingParams() throws Exception {
        CreateProblemDto data = getData();
        GetProblemDto getProblemDto = new GetProblemDto();
        getProblemDto.setName(data.getName());
        Mockito.when(problemService.getAll(any(), any(), anyInt(), anyInt())).thenReturn(List.of(getProblemDto));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/")
                        .param("visible", "123")
                        .param("search", "problem")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/")
                        .param("search", "problem")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/")
                        .param("search", "problem")
                        .param("page", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/")
                        .param("search", "problem")
                        .param("page", "0")
                        .param("pageSize", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getByIdShouldSend200AndReturnProblemForValidData() throws Exception {
        CreateProblemDto data = getData();
        GetProblemDto getProblemDto = new GetProblemDto();
        getProblemDto.setName(data.getName());
        Mockito.when(problemService.get(any())).thenReturn(getProblemDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name", is(data.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getByIdShouldSend400ForInvalidOrMissingId() throws Exception {
        Mockito.when(problemService.get(any())).thenThrow(InvalidIdException.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/a"))
                .andExpect(status().isBadRequest());

        ServletException ex = assertThrows(ServletException.class, () -> mockMvc.perform(MockMvcRequestBuilders
                .get("/api/problem/13")));

        assertEquals(ex.getRootCause().getClass(), InvalidIdException.class);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminGetByIdShouldSend200AndReturnProblemForValidData() throws Exception {
        CreateProblemDto data = getData();
        AdminGetProblemDto getProblemDto = new AdminGetProblemDto();
        getProblemDto.setName(data.getName());
        Mockito.when(problemService.adminGet(any())).thenReturn(getProblemDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/admin/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name", is(data.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminGetByIdShouldSend400ForInvalidOrMissingId() throws Exception {
        Mockito.when(problemService.adminGet(any())).thenThrow(InvalidIdException.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/problem/admin/a"))
                .andExpect(status().isBadRequest());

        ServletException ex = assertThrows(ServletException.class, () -> mockMvc.perform(MockMvcRequestBuilders
                .get("/api/problem/admin/13")));

        assertEquals(ex.getRootCause().getClass(), InvalidIdException.class);
    }
}
