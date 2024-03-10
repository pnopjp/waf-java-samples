package org.pnop.waf.sample.act.sb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
public class HealthEndpointApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthEndpointApplication.class, args);
    }

    @Bean
    SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(authorize -> {
                authorize.requestMatchers("/health/**").permitAll();
                authorize.requestMatchers("/actuator/**").permitAll();
                authorize.anyRequest().authenticated();
            }).build();
    }

}
