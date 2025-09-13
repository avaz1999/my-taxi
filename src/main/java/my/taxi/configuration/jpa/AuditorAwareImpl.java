package my.taxi.configuration.jpa;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Created by Avaz Absamatov
 * Date: 9/13/2025
 */
public class AuditorAwareImpl implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.empty();
    }
}
