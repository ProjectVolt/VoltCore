package io.github.capure.voltcore.dto;

import io.github.capure.voltcore.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetUserDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String avatar;
    private String github;
    private String school;
    private Integer acceptedSubmissions;
    private Integer submissionCount;
    private Integer totalScore;

    public static GetUserDto getFromUser(User user) {
        return new GetUserDto(user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getAvatar(),
                user.getGithub(),
                user.getSchool(),
                user.getAcceptedSubmissions(),
                user.getSubmissionCount(),
                user.getTotalScore());
    }
}
