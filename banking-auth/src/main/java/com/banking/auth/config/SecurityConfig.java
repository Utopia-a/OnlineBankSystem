package com.banking.auth.config;

import com.banking.auth.filter.JwtAuthenticationFilter;
import com.banking.auth.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security 核心配置
 * - 无状态 JWT 认证
 * - 白名单路径放行
 * - 统一 401/403 JSON 响应
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    /** 完全公开的路径（无需认证） */
    private static final String[] WHITE_LIST = {
            "/auth/register",
            "/auth/login",
            "/auth/verify-email",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/refresh-token",
            "/auth/resend-otp",
            // Swagger UI
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            // H2 Console (仅测试环境)
            "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（JWT 无状态，无需 CSRF 保护）
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用 CORS（前端开发时按需打开）
            .cors(AbstractHttpConfigurer::disable)
            // 无状态 Session
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 路径权限配置
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(WHITE_LIST).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            // 认证提供者
            .authenticationProvider(authenticationProvider())
            // JWT 过滤器在 UsernamePasswordAuthenticationFilter 之前
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 统一 401 响应
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler())
            )
            // H2 Console iframe（仅测试环境使用）
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /** 未认证（401）统一 JSON 响应 */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "未认证，请先登录");
            body.put("timestamp", LocalDateTime.now().toString());
            new ObjectMapper().writeValue(response.getOutputStream(), body);
        };
    }

    /** 无权限（403）统一 JSON 响应 */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "无权限访问该资源");
            body.put("timestamp", LocalDateTime.now().toString());
            new ObjectMapper().writeValue(response.getOutputStream(), body);
        };
    }
}
