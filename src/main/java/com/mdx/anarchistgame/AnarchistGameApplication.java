package com.mdx.anarchistgame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnarchistGameApplication {

    public static void main(String[] args) {
        var amos = "AMOS ".matches("^([0-9]+\\.?[0-9]*|\\.[0-9]+)$");
        SpringApplication.run(AnarchistGameApplication.class, args);
    }

}
