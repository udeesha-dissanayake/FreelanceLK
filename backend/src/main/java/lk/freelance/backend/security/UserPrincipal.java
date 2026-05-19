package lk.freelance.backend.security;

// --- CRITICAL IMPORTS ---
import lk.freelance.backend.entity.User;
import lk.freelance.backend.enums.UserStatus;
import org.springframework.lang.NonNull; // Required for @NullMarked compatibility
// ------------------------

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Custom UserPrincipal class for Spring Security authentication
 * Wraps user information in JWT token
 */
@Data
public class UserPrincipal implements UserDetails {

    private UUID userId;
    private String email;
    private String password;
    private String role;
    private boolean enabled;
    private boolean emailVerified;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(
            UUID userId,
            String email,
            String password,
            String role,
            boolean enabled,
            boolean emailVerified,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        // enabled = account is not INACTIVE or SUSPENDED (PENDING_VERIFICATION is still usable)
        boolean enabled = user.getStatus() != UserStatus.INACTIVE
                && user.getStatus() != UserStatus.SUSPENDED;

        return new UserPrincipal(
                user.getUserId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().name(),
                enabled,
                user.getEmailVerified(),
                authorities
        );
    }

    @Override
    @NonNull
    public String getUsername() {
        return email;
    }

    @Override
    @NonNull
    public String getPassword() {
        return password;
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // locking handled separately via UserStatus.SUSPENDED check in AuthService
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}