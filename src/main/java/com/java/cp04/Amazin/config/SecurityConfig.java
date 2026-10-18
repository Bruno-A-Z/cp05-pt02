package com.java.cp04.Amazin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            boolean admin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(request.getContextPath() + (admin ? "/admin" : "/cliente"));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Públicas
                        .requestMatchers("/", "/req/login", "/req/cadastro", "/acesso-negado",
                                "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()

                        // API do Banco Tranquilo: protegida por chave de API (X-API-KEY), não por login
                        .requestMatchers("/banco-tranquilo/api/**").permitAll()

                        // Somente ADMIN (as mais específicas vêm primeiro)
                        .requestMatchers("/admin", "/admin/**",
                                "/web/itens/novo", "/web/itens/*/editar", "/web/itens/*/deletar",
                                "/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/web/itens").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/item/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/item/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/item/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/item/**").hasRole("ADMIN")

                        // USER e ADMIN
                        .requestMatchers("/cliente", "/carrinho", "/carrinho/**", "/pedidos", "/pedidos/**",
                                "/api/compras", "/api/compras/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/web/itens", "/web/itens/*", "/item", "/item/**")
                        .hasAnyRole("USER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/req/login")
                        .loginProcessingUrl("/req/login")
                        .successHandler(loginSuccessHandler())
                        .failureUrl("/req/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/req/login?logout")
                        .permitAll()
                )
                .httpBasic(Customizer.withDefaults()) // permite testar /item no Postman/Insomnia
                .csrf(csrf -> csrf.ignoringRequestMatchers("/item/**", "/api/**", "/banco-tranquilo/api/**"))
                .exceptionHandling(ex -> ex.accessDeniedPage("/acesso-negado"));

        return http.build();
    }
}
