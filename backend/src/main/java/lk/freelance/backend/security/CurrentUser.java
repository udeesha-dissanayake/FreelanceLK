package lk.freelance.backend.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;

/**
 * Custom annotation to inject current authenticated user
 * Usage: @CurrentUser UserPrincipal currentUser
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}