package io.github.capure.voltcore.controller;

import io.github.capure.voltcore.dto.UserLoginDto;
import io.github.capure.voltcore.dto.UserRegisterDto;
import io.github.capure.voltcore.exception.*;
import io.github.capure.voltcore.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @DeleteMapping("/{id}")
    public String delete(HttpServletResponse response, @PathVariable("id") @NotNull @Min(1) Long id) {
        try {
            userService.delete(id);
            response.setStatus(200);
            return "Deleted";
        } catch (FailedDeletionException e) {
            response.setStatus(400);
            return "Invalid id";
        }
    }

    @PostMapping("/login")
    public void login(HttpServletResponse response, @Valid @RequestBody UserLoginDto loginData) {
        try {
            String token = userService.login(loginData);
            response.setHeader("Authorization", "Bearer " + token);
            response.setStatus(201);
        } catch (FailedLoginException e) {
            response.setStatus(401);
        }
    }

    @PostMapping("/register")
    @ResponseBody
    public String register(HttpServletResponse response, @Valid @RequestBody UserRegisterDto registerData) {
        try {
            userService.register(registerData);
            response.setStatus(201);
            return "Created";
        } catch (UsernameAlreadyInUseException e) {
            response.setStatus(409);
            return "Username already in use";
        } catch (EmailAlreadyInUseException e) {
            response.setStatus(409);
            return "Email already in use";
        } catch (RegistrationDisabledException e) {
            response.setStatus(403);
            return "Registration disabled";
        } catch (FailedRegistrationException e) {
            response.setStatus(500);
            return "Internal server error";
        }
    }
}
