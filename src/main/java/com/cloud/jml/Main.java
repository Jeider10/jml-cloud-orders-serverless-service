package com.cloud.jml;

import com.cloud.jml.banner.DynamicBanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Slf4j
@SpringBootApplication
public class Main {

    public static final String MICRO_NAME = "M i c r o - O r d e r s";

    public static void main(String[] args) {

        log.info("Hello, World!");

        SpringApplication app = new SpringApplication(Main.class);

        app.setBanner(new DynamicBanner(MICRO_NAME));

        app.run(args);
    }
}
