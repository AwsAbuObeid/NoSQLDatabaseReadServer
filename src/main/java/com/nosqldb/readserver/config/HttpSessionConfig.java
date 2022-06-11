package com.nosqldb.readserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@Configuration
public class HttpSessionConfig {

    private int sessionCount =0;
    public int getNumberOfSessions() { return sessionCount;}

    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent hse) {
                sessionCount++;
            }
            @Override
            public void sessionDestroyed(HttpSessionEvent hse) {
                sessionCount--;
            }
        };
    }
} 