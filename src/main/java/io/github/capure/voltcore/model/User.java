package io.github.capure.voltcore.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Table(name = "volt_user")
@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String username;
    @Nullable
    private String password;
    @NotNull
    private String email;
    @NotNull
    private Boolean enabled;
    @NotNull
    private String role;
    @NotNull
    private String avatar;
    @Nullable
    private String github;
    @Nullable
    private String school;
    @NotNull
    private Integer acceptedSubmissions;
    @NotNull
    private Integer submissionCount;
    @NotNull
    private Integer totalScore;
    @OneToMany(mappedBy = "addedBy")
    private Set<Problem> problems;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return password != null;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
