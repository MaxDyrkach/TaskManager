package io.mds.hty.taskmanager.conf;

import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.repo.EmployeeRepo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(EmployeeRepo repository) {
        return username -> {
            Optional<Employee> userOp = repository.findEmployeeByUserNameIs(username);
            return userOp.orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, EmployeeRepo repository) throws Exception {
        return http
                .requiresChannel(c-> c.anyRequest().requiresSecure())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .userDetailsService(userDetailsService(repository))
                .authorizeHttpRequests(requests -> {
                    requests.anyRequest().authenticated();
                })
                .httpBasic(conf -> conf.realmName("realm"))
                .exceptionHandling(h -> {
                    h.authenticationEntryPoint((rq,rs, e) -> rs.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage()))
                            .accessDeniedHandler((rq, rs, e) -> rs.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage()));
                })
                .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/h2-console/**");
    }



}
