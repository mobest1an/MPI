package com.iverpa.mpi.config;

import com.iverpa.mpi.security.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

@Configuration
public class SecurityConfig {

    @Value("${public.key.filename}")
    private String publicKeyFileName;

    @Bean
    public SecurityFilterChain web(HttpSecurity http) throws Exception {
        byte[] key = Files.readAllBytes(Paths.get(publicKeyFileName));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        AuthTokenFilter authTokenFilter = new AuthTokenFilter(publicKey);
        http
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry ->
                        {
                            registry.requestMatchers("/api/v1/recruit/**").hasAuthority("recruit");
                            registry.requestMatchers("/api/v1/escort/**").hasAuthority("escort");
                            registry.requestMatchers("/api/v1/commissar/**").hasAuthority("commissar");
                            registry.requestMatchers("/api/v1/admin/**").hasAuthority("admin");
                            registry.requestMatchers("/api/v1/auth/**").permitAll();

                            registry.anyRequest().denyAll();
                        }
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
