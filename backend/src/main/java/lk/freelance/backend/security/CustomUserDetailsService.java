package lk.freelance.backend.security;

import lk.freelance.backend.entity.User;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lk.freelance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull; // Required for @NullMarked compatibility
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Custom UserDetailsService for Spring Security
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    @NonNull // Satisfies @NullMarked override requirement
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );

        // Ensure UserPrincipal.create() accepts lk.freelance.backend.entity.User
        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "id", userId)
                );

        return UserPrincipal.create(user);
    }
}