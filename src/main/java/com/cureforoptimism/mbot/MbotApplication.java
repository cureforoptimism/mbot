package com.cureforoptimism.mbot;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@AllArgsConstructor
public class MbotApplication {
  public static void main(String[] args) {
    SpringApplication.run(MbotApplication.class, args);
  }
}
