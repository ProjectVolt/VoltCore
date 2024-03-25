package io.github.capure.voltcore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.capure.voltcore.config.SecurityConfig;
import io.github.capure.voltcore.dto.CreateTagDto;
import io.github.capure.voltcore.dto.GetTagDto;
import io.github.capure.voltcore.dto.GetUserDto;
import io.github.capure.voltcore.exception.FailedCreateException;
import io.github.capure.voltcore.service.TagService;
import io.github.capure.voltcore.service.TagServiceTest;
import io.github.capure.voltcore.service.UserDetailsServiceImpl;
import io.github.capure.voltcore.service.UserService;
import io.github.capure.voltcore.util.JwtUtil;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.is;

@WebMvcTest
@Import(SecurityConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TagServiceTest.class, TagController.class, JwtUtil.class, UserDetailsServiceImpl.class})
public class TagControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @WithMockUser(roles = {"STAFF"})
    public void createShouldReturnGetTagDtoForValidData() throws Exception {
        CreateTagDto postData = new CreateTagDto();
        postData.setName("TAG");
        when(tagService.create(any())).thenReturn(new GetTagDto(1L, "TAG"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tag/")
                        .content(asJsonString(postData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name", is("TAG")));
    }

    @Test
    @WithMockUser(roles = {"STAFF"})
    public void createShouldReturn400ForInvalidData() throws Exception {
        CreateTagDto postData = new CreateTagDto();
        postData.setName("T");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tag/")
                        .content(asJsonString(postData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"STAFF"})
    public void createShouldReturn500ForDbError() throws Exception {
        CreateTagDto postData = new CreateTagDto();
        postData.setName("TAG");
        when(tagService.create(any())).thenThrow(FailedCreateException.class);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/tag/")
                        .content(asJsonString(postData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser()
    public void getAllShouldSend200AndReturnListOfTagsForValidParams() throws Exception {
        Mockito.when(tagService.getAll(any(), anyInt(), anyInt())).thenReturn(List.of(new GetTagDto(1L, "TAG")));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tag/")
                        .param("search", "tag")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name", is("TAG")));
    }

    @Test
    @WithMockUser
    public void getAllShouldSend400ForInvalidOrMissingParams() throws Exception {
        Mockito.when(tagService.getAll(any(), anyInt(), anyInt())).thenReturn(List.of(new GetTagDto(1L, "TAG")));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tag/")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tag/")
                        .param("search", "tag")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tag/")
                        .param("search", "tag")
                        .param("page", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/tag/")
                        .param("search", "tag")
                        .param("page", "0")
                        .param("pageSize", "invalid"))
                .andExpect(status().isBadRequest());
    }
}
