package com.college.receipt.config;

import com.college.receipt.service.User.MyUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    public static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    @Autowired
    private MyUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/create_recipe") // Отключение CSRF для конкретного эндпоинта
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/login", "/user/logout", "/resources/**", "/user/registration", "/create_recipe").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Доступ только для роли ADMIN
                        .requestMatchers(HttpMethod.GET, "/user/login","create_recipe").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/login/done","/create_recipe").permitAll()
                        .requestMatchers("/anonymous*").anonymous() // Доступ только анонимным пользователям
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/user/login")
                        .loginProcessingUrl("/user/login/done")
                        .failureHandler((request, response, exception) -> {
                            logger.error("Authentication failed: {}", exception.getMessage());
                            response.sendRedirect("/user/login?error=true");
                        })
                        .successHandler((request, response, authentication) -> {
                            logger.info("Authentication successful for user: {}", authentication.getName());
                            response.sendRedirect("/");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/logout.html?logSucc=true")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(daoAuthenticationProvider())
                .build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // На случай  если будут проблемы с регистрацией
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll() // Разрешить все запросы
//                )
//                .httpBasic(httpBasic -> httpBasic.disable()) // Отключить базовую аутентификацию
//                .formLogin(form -> form.disable()) // Отключить форму входа
//                .logout(logout -> logout.disable()); // Отключить логаут
//        return http.build();
//    }
}
