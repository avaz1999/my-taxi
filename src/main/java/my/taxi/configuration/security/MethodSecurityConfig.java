package my.taxi.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@Configuration
public class MethodSecurityConfig {
    @Bean
    RoleHierarchy roleHierarchy() {
        String hierarchy = "ROLE_ADMIN > ROLE_MANAGER\nROLE_MANAGER > ROLE_OPERATOR\nROLE_OPERATOR > ROLE_DRIVER\nROLE_DRIVER > ROLE_CLIENT";
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl(hierarchy);
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }
}
