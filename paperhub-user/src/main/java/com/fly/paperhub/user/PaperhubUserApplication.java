package com.fly.paperhub.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PaperhubUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperhubUserApplication.class, args);
    }

}
