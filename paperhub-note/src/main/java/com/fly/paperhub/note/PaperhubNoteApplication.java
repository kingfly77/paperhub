package com.fly.paperhub.note;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.fly.paperhub.note.dao")
public class PaperhubNoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperhubNoteApplication.class, args);
    }

}
