package pl.pb.tas.teaching_allocation_system.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary security baseline for the API skeleton.
 *
 * <p>Authentication rules will be introduced with the authentication
 * implementation. Until then, endpoints remain reachable during local
 * development.</p>
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(authorize -> authorize
						.anyRequest().permitAll());

		return http.build();
	}
}
