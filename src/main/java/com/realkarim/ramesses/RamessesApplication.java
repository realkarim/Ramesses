package com.realkarim.ramesses;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RamessesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RamessesApplication.class, args);
    }

}
