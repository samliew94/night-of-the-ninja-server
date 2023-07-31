package com;

import com.filter.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class NightOfTheNinja {

    public static void main(String[] args) {
        SpringApplication.run(NightOfTheNinja.class, args);
    }

}

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Autowired
    MyUserRepository userRepository;

    @Bean
    public RateLimiter rateLimiterFilter() {
        return new RateLimiter();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .addFilterBefore(rateLimiterFilter(), UsernamePasswordAuthenticationFilter.class)
                .cors().and()
                .csrf(x -> x.disable())
                .authorizeHttpRequests(x -> {
                    x.requestMatchers("/", "user/login", "user/logout", "vite.svg", "index.html", "assets/**").permitAll();
//                    x.requestMatchers(PathRequest.toH2Console()).permitAll();
                    x.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
//					x.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
                    x.anyRequest().authenticated();
                })
                .headers(x -> x.frameOptions(y -> y.sameOrigin()))

        ;

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("index.html", "assets/**", "vite.svg");
    }


    @Bean
    AuthenticationProvider authenticationProvider() {

        return new AuthenticationProvider() {

            @Override
            public Authentication authenticate(Authentication auth) throws AuthenticationException {

                System.err.println("authenticating with custom provider...");

                String username = auth.getName();
                if (username == null || username.isBlank())
                    throw new BadCredentialsException("bad username");

                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, "", AuthorityUtils.createAuthorityList("USER"));
                SecurityContextHolder.getContext().setAuthentication(token);

                return token;
            }

            @Override
            public boolean supports(Class<?> auth) {
                return auth.equals(UsernamePasswordAuthenticationToken.class);
            }
        };
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

@Configuration
class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST")
                .allowCredentials(true)
        ;
    }
}
