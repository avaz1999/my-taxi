package my.taxi.configuration.jpa;

import my.taxi.entities.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Created by Avaz Absamatov
 * Date: 9/13/2025
 */
public class AuditorAwareImpl implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return Optional.of(user.getId());
        }
        return Optional.empty();
    }
}
