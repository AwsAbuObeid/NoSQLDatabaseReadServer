package com.nosqldb.readserver.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


/**
 * APISecurityConfig Class is used to implement spring security into
 * the application, it uses the APIkeyRecord to authenticate the users.
 * each user is started a session that lasts for 15 minutes if left idle.
 */
@Configuration
@EnableWebSecurity
public class APISecurity extends WebSecurityConfigurerAdapter {
    @Autowired
    APIkeyRecord apIkeyRecord;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        APIKeyAuthFilter filter = new APIKeyAuthFilter("x-api-key");
        filter.setAuthenticationManager(authentication -> {
            String key = (String) authentication.getPrincipal();
            if (!apIkeyRecord.isValidKey(key)) {
                throw new BadCredentialsException("Missing or invalid APIKey");
            }
            authentication.setAuthenticated(true);
            return authentication;
        });
        httpSecurity.
                addFilter(filter).requestMatchers().
                antMatchers("/**").
                and().
                authorizeRequests().
                anyRequest().
                authenticated().and().csrf().disable();
    }

}