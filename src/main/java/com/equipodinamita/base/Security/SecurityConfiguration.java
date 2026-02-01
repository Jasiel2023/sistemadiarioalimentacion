package com.equipodinamita.base.Security;

/*import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;

import com.fasterxml.jackson.databind.JsonSerializable.Base;
import com.nimbusds.jose.JWSAlgorithm;
import com.vaadin.flow.spring.security.VaadinWebSecurity;

@EnableWebSecurity
@Configuration

public class SecurityConfiguration  extends VaadinWebSecurity{
    
    //public static final String LOGIN_URL = "/login";
    public static final String LOGOUT_URL = "/";

    @Value ("$(jwt.auth.secret)")
    private String authSecret;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{
       http
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/AnimalView", "/persona-list", "/adopcion", "/Seguimiento").hasRole("admin").
            requestMatchers("/AnimalCardView").hasAnyRole("admin", "user")
        );
    
    
    super.configure(http);  

    

    setLoginView(http, "/login", "/");

     
      
    }

    @Override
    protected void configure(WebSecurity web) throws Exception {
      
        web.ignoring().requestMatchers(VaadinWebSecurity.getDefaultHttpSecurityPermitMatcher()).
        requestMatchers(new AntPathRequestMatcher("/images/**"))
        .requestMatchers(antMatchers("/static/**"));
        super.configure(web);
    }

}
*/