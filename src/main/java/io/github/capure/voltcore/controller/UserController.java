package io.github.capure.voltcore.controller;

import io.github.capure.voltcore.dto.GetUserDto;
import io.github.capure.voltcore.dto.PutUserDto;
import io.github.capure.voltcore.dto.UserLoginDto;
import io.github.capure.voltcore.dto.UserRegisterDto;
import io.github.capure.voltcore.dto.admin.AdminGetUserDto;
import io.github.capure.voltcore.dto.admin.AdminPutUserDto;
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

    @PutMapping("/admin/{id}")
    public String adminUpdate(HttpServletResponse response, @PathVariable("id") @NotNull @Min(1) Long id, @Valid @RequestBody AdminPutUserDto data) throws InvalidIdException {
        try {
            userService.adminUpdate(id, data);
            response.setStatus(200);
            return "OK";
        } catch (FailedUpdateException e) {
            response.setStatus(500);
            return "Internal server error";
        }
    }

    @GetMapping("/admin/{id}")
    public AdminGetUserDto adminGetById(HttpServletResponse response, @PathVariable("id") @NotNull @Min(1) Long id) throws InvalidIdException {
        response.setStatus(200);
        return userService.adminGet(id);
    }

    @PutMapping("/{id}")
    public String update(HttpServletResponse response, @PathVariable("id") @NotNull @Min(1) Long id, @Valid @RequestBody PutUserDto data) throws InvalidIdException {
        try {
            userService.update(id, data);
            response.setStatus(200);
            return "OK";
        } catch (FailedUpdateException e) {
            response.setStatus(500);
            return "Internal server error";
        }
    }

    @GetMapping("/{id}")
    public GetUserDto getById(HttpServletResponse response, @PathVariable("id") @NotNull @Min(1) Long id) throws InvalidIdException {
        response.setStatus(200);
        return userService.get(id);
    }

    @DeleteMapping("/{id}")
    public String delete(HttpServletResponse response, @PathVariable("id") @NotNull @Min(1) Long id) throws InvalidIdException {
        try {
            userService.delete(id);
            response.setStatus(200);
            return "Deleted";
        } catch (FailedDeletionException e) {
            throw new InvalidIdException();
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
