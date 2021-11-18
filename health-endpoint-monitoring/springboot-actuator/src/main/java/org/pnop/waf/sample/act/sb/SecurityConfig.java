package org.pnop.waf.sample.act.sb;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//
//        var filter = new HeaderCheckFilter("X-TEST-KEY");
//        filter.setAuthenticationManager(new Hoge());
//        http
//            .antMatcher("/actuator/**")
//            .addFilter(filter)
//            .authorizeRequests()
//            .anyRequest()
//            .authenticated()
//            .and()
//            .exceptionHandling()
//            .authenticationEntryPoint(new AuthenticationEntryPoint() {
//                @Override
//                public void commence(HttpServletRequest request, HttpServletResponse response,
//                    AuthenticationException authException) throws IOException, ServletException {
//                    log.debug("Pre-authenticated entry point called. Rejecting access");
//                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
//                }
//            });
//
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    public class Hoge implements AuthenticationManager {

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String principal = (String) authentication.getPrincipal();
            if (principal.equals("PASS")) {
                authentication.setAuthenticated(true);
            } else {
                throw new BadCredentialsException("credential error");
            }
            return authentication;
        }
    }

    public class HeaderCheckFilter extends AbstractPreAuthenticatedProcessingFilter {
        private String headerName;

        public HeaderCheckFilter(String headerName) {
            this.headerName = headerName;
        }

        @Override
        protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
            return request.getHeader(headerName);
        }

        @Override
        protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
            return "";
        }
    }
}
