package io.github.capure.voltcore.dto.admin;

import io.github.capure.voltcore.model.User;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminGetUserDto {
    @Nonnull
    private Long id;
    @Nonnull
    private String username;
    @Nonnull
    private String email;
    @Nonnull
    private Boolean enabled;
    @Nonnull
    private String role;
    @Nonnull
    private String avatar;
    @Nullable
    private String github;
    @Nullable
    private String school;
    @Nonnull
    private Integer acceptedSubmissions;
    @Nonnull
    private Integer submissionCount;
    @Nonnull
    private Integer totalScore;

    public static AdminGetUserDto getFromUser(User user) {
        return new AdminGetUserDto(user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRole(),
                user.getAvatar(),
                user.getGithub(),
                user.getSchool(),
                user.getAcceptedSubmissions(),
                user.getSubmissionCount(),
                user.getTotalScore());
    }
}
