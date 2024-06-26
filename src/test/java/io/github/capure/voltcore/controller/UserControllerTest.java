package io.github.capure.voltcore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.capure.voltcore.config.SecurityConfig;
import io.github.capure.voltcore.dto.GetUserDto;
import io.github.capure.voltcore.dto.PutUserDto;
import io.github.capure.voltcore.dto.UserLoginDto;
import io.github.capure.voltcore.dto.UserRegisterDto;
import io.github.capure.voltcore.dto.admin.AdminGetUserDto;
import io.github.capure.voltcore.dto.admin.AdminPutUserDto;
import io.github.capure.voltcore.exception.*;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.service.UserDetailsServiceImpl;
import io.github.capure.voltcore.service.UserService;
import io.github.capure.voltcore.util.GlobalExceptionHandler;
import io.github.capure.voltcore.util.JwtUtil;
import jakarta.servlet.ServletException;
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
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

@WebMvcTest
@Import(SecurityConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JwtUtil.class, UserController.class, UserService.class, UserDetailsServiceImpl.class})
public class UserControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static String repeatChar(int size, char ch) {
        return String.format("%0" + size + "d", 0).replace('0', ch);
    }

    @Test
    public void loginShouldValidateData() throws Exception {
        UserLoginDto loginData1 = new UserLoginDto("ab", "password");
        UserLoginDto loginData2 = new UserLoginDto("test", "pass");
        UserLoginDto loginData3 = new UserLoginDto("test", null);
        Consumer<UserLoginDto> test = (loginData) -> {
            try {
                mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/user/login")
                                .content(asJsonString(loginData))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        test.accept(loginData1);
        test.accept(loginData2);
        test.accept(loginData3);
    }

    @Test
    public void registerShouldValidateData() throws Exception {
        UserRegisterDto userRegisterData1 = new UserRegisterDto("ab", "password", "example@example.com", null, null);
        UserRegisterDto userRegisterData2 = new UserRegisterDto("test", "pass", "example@example.com", null, null);
        UserRegisterDto userRegisterData3 = new UserRegisterDto("test", "password", "exampleexample.com", null, null);
        UserRegisterDto userRegisterData4 = new UserRegisterDto("test", "password", "example@example.com", repeatChar(51, 'a'), null);
        UserRegisterDto userRegisterData5 = new UserRegisterDto("test", "password", "example@example.com", null, repeatChar(101, 'a'));
        Consumer<UserRegisterDto> test = (registerData) -> {
            try {
                mockMvc.perform(MockMvcRequestBuilders
                                .post("/api/user/register")
                                .content(asJsonString(registerData))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        test.accept(userRegisterData1);
        test.accept(userRegisterData2);
        test.accept(userRegisterData3);
        test.accept(userRegisterData4);
        test.accept(userRegisterData5);
    }

    @Test
    public void loginShouldSend401ForInvalidData() throws Exception {
        Mockito.when(userService.login(any())).thenThrow(FailedLoginException.class);
        UserLoginDto userLogin = new UserLoginDto("tester", "password");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/user/login")
                        .content(asJsonString(userLogin))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginShouldSendTokenForValidData() throws Exception {
        Mockito.when(userService.login(any())).thenReturn("token");
        UserLoginDto userLogin = new UserLoginDto("tester", "password");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/user/login")
                        .content(asJsonString(userLogin))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(header().string("Authorization", "Bearer token"));
    }

    @Test
    public void registerShouldSend201ForValidData() throws Exception {
        UserRegisterDto userRegisterData = new UserRegisterDto("test", "password", "example@example.com", null, null);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/user/register")
                        .content(asJsonString(userRegisterData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void registerShouldSend409ForTakenUsernameOrEmail() throws Exception {
        UserRegisterDto userRegisterData = new UserRegisterDto("test", "password", "example@example.com", null, null);
        Mockito.doThrow(UsernameAlreadyInUseException.class).when(userService).register(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/user/register")
                        .content(asJsonString(userRegisterData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        Mockito.doThrow(EmailAlreadyInUseException.class).when(userService).register(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/user/register")
                        .content(asJsonString(userRegisterData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void registerShouldSend403ForDisabledRegistration() throws Exception {
        UserRegisterDto userRegisterData = new UserRegisterDto("test", "password", "example@example.com", null, null);
        Mockito.doThrow(RegistrationDisabledException.class).when(userService).register(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/user/register")
                        .content(asJsonString(userRegisterData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void registerShouldSend500ForUnexpectedError() throws Exception {
        UserRegisterDto userRegisterData = new UserRegisterDto("test", "password", "example@example.com", null, null);
        Mockito.doThrow(FailedRegistrationException.class).when(userService).register(any());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/user/register")
                        .content(asJsonString(userRegisterData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    public void deleteShouldSend400ForInvalidId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/user/0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/user/test"))
                .andExpect(status().isBadRequest());

        Mockito.doThrow(FailedDeletionException.class).when(userService).delete(1L);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/user/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void deleteShouldSend200ForValidId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getShouldReturnGetUserDto() throws Exception {
        Mockito.when(userService.get(1L)).thenReturn(GetUserDto.getFromUser(getUser()));
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username", is(getUser().getUsername())));
    }

    @Test
    @WithMockUser
    public void updateShouldSend200ForValidData() throws Exception {
        PutUserDto putData = new PutUserDto("abcdef", "Capure", "Volt LO");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/user/" + getUser().getId())
                        .content(asJsonString(putData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void updateShouldSend500ForDatabaseError() throws Exception {
        PutUserDto putData = new PutUserDto("abcdef", "Capure", "Volt LO");
        Mockito.doThrow(FailedUpdateException.class).when(userService).update(any(), any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/user/" + getUser().getId())
                        .content(asJsonString(putData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminGetByIdShouldReturnAdminGetUserDto() throws Exception {
        Mockito.when(userService.adminGet(getUser().getId())).thenReturn(AdminGetUserDto.getFromUser(getUser()));
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/admin/" + getUser().getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username", is(getUser().getUsername())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminUpdateShouldSend200ForValidData() throws Exception {
        AdminPutUserDto putData = new AdminPutUserDto();
        putData.setGithub("Capure");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/user/admin/" + getUser().getId())
                        .content(asJsonString(putData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminUpdateShouldSend500ForDatabaseError() throws Exception {
        AdminPutUserDto putData = new AdminPutUserDto();
        Mockito.doThrow(FailedUpdateException.class).when(userService).adminUpdate(any(), any());

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/user/admin/" + getUser().getId())
                        .content(asJsonString(putData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminUpdateShouldSend400ForInvalidRole() throws Exception {
        AdminPutUserDto putData = new AdminPutUserDto();
        putData.setRole("invalid");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/user/admin/" + getUser().getId())
                        .content(asJsonString(putData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void getAllShouldSend200AndReturnListOfUsersForValidParams() throws Exception {
        Mockito.when(userService.getAll(any(), anyInt(), anyInt())).thenReturn(List.of(GetUserDto.getFromUser(getUser())));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/")
                        .param("search", "username")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].username", is(getUser().getUsername())));
    }

    @Test
    @WithMockUser
    public void getAllShouldSend400ForInvalidOrMissingParams() throws Exception {
        Mockito.when(userService.getAll(any(), anyInt(), anyInt())).thenReturn(List.of(GetUserDto.getFromUser(getUser())));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/")
                        .param("search", "username")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/")
                        .param("search", "username")
                        .param("page", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/")
                        .param("search", "username")
                        .param("page", "0")
                        .param("pageSize", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminGetAllShouldSend200AndReturnListOfUsersForValidParams() throws Exception {
        Mockito.when(userService.adminGetAll(any(), anyInt(), anyInt(), any())).thenReturn(List.of(AdminGetUserDto.getFromUser(getUser())));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/admin/")
                        .param("search", "username")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].username", is(getUser().getUsername())));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/admin/")
                        .param("search", "username")
                        .param("page", "0")
                        .param("pageSize", "10")
                        .param("enabled", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].username", is(getUser().getUsername())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void adminGetAllShouldSend400ForInvalidOrMissingParams() throws Exception {
        Mockito.when(userService.adminGetAll(any(), anyInt(), anyInt(), any())).thenReturn(List.of(AdminGetUserDto.getFromUser(getUser())));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/admin/")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/admin/")
                        .param("search", "username")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/admin/")
                        .param("search", "username")
                        .param("page", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/user/admin/")
                        .param("search", "username")
                        .param("page", "0")
                        .param("pageSize", "invalid"))
                .andExpect(status().isBadRequest());
    }
}
