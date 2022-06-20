package com.nosqldb.readserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class ReadserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReadserverApplication.class, args);
    }

}
