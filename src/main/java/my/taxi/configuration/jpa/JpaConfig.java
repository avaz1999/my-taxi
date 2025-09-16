package my.taxi.configuration.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Created by Avaz Absamatov
 * Date: 9/13/2025
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {
    @Bean
    public AuditorAware<Long> auditorAware() {
        return new AuditorAwareImpl();
    }
}
