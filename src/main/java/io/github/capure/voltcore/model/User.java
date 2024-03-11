package io.github.capure.voltcore.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Table(name = "volt_user")
@Entity
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class User implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Nonnull
    private String username;
    @Nullable
    private String password;
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
