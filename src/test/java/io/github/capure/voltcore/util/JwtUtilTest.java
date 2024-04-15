package io.github.capure.voltcore.util;

import io.github.capure.voltcore.model.User;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.FixedClock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;

import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JwtUtil.class}, initializers = {ConfigDataApplicationContextInitializer.class})
public class JwtUtilTest {
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String secret;

    private int jwtExpirationInMs;

    @Value("${jwt.expiration-time}")
    public void setJwtExpirationInMs(String jwtExpirationInMs) {
        this.jwtExpirationInMs = Integer.parseInt(jwtExpirationInMs);
    }

    private UserDetails getUser() {
        return new User(1L,
                "tester",
                "password1",
                "tester@example.com",
                true,
                "ROLE_ADMIN",
                "https://example.com",
                "https://github.com/Capure",
                null,
                0,
                0,
                0,
                Set.of(),
                Set.of());
    }

    @Test
    public void shouldGenerateTokenForValidUserDetails() {
        UserDetails userDetails = getUser();

        String token = assertDoesNotThrow(() -> jwtUtil.generate(userDetails));
        assertEquals(userDetails.getUsername(), Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject());
    }

    @Test
    public void shouldGetUsernameFromGeneratedToken() {
        UserDetails userDetails = getUser();

        String token = assertDoesNotThrow(() -> jwtUtil.generate(userDetails));
        assertEquals(userDetails.getUsername(), jwtUtil.getUsername(token));
    }

    @Test
    public void shouldThrowForExpiredToken() {
        JwtUtil mockedParserJwtUtil = Mockito.spy(jwtUtil);
        JwtParser parser = Jwts.parser().setSigningKey(secret);
        Clock clock = new FixedClock(new Date(System.currentTimeMillis() + jwtExpirationInMs));
        parser.setClock(clock);
        Mockito.when(mockedParserJwtUtil.getParser()).thenReturn(parser);

        String token = assertDoesNotThrow(() -> mockedParserJwtUtil.generate(getUser()));
        assertThrows(ExpiredJwtException.class, () -> mockedParserJwtUtil.validate(token));
    }

    @Test
    public void shouldReturnCorrectRole() {
        UserDetails userDetails = getUser();

        String token = assertDoesNotThrow(() -> jwtUtil.generate(userDetails));
        assertThat(jwtUtil.getRoles(token)).contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
