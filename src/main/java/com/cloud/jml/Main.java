package com.cloud.jml;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Slf4j
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        log.info("Hello, World!");
        SpringApplication.run(Main.class, args);
    }
}
