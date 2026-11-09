package com.example.datn.config;

import com.example.datn.security.JwtAuthConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())                       // API dùng token, không dùng cookie phiên
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**", "/api/public/**", "/uploads/**").permitAll()

                        // Quản lý nhân viên: chỉ quản trị viên
                        .requestMatchers("/api/admin/nhan-vien/**", "/api/admin/vai-tro/**").hasRole("ADMIN")

                        // Sản phẩm và thuộc tính
                        .requestMatchers("/api/admin/san-pham/**", "/api/admin/anh/**", "/api/admin/danh-muc/**",
                                "/api/admin/mau-sac/**", "/api/admin/kich-thuoc/**", "/api/admin/chat-lieu/**",
                                "/api/admin/thuong-hieu/**").hasAnyRole("ADMIN", "MANAGER", "WAREHOUSE")

                        // Hóa đơn (sẽ làm sau)
                        .requestMatchers("/api/admin/hoa-don/**").hasAnyRole("ADMIN", "MANAGER", "SALES")

                        // Khách hàng
                        .requestMatchers("/api/admin/khach-hang/**").hasAnyRole("ADMIN", "MANAGER", "SUPPORT")

                        // Mặc định cho mọi đường dẫn admin còn lại (thống kê, khuyến mãi, voucher...)
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MANAGER")

                        // Khu vực của khách hàng (profile, đơn hàng của tôi, đặt hàng...)
                        .requestMatchers("/api/user/**").hasRole("KHACH_HANG")

                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtAuthConverter)));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:5173"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", c);
        return source;
    }
}
