package com.kozarenko.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.kozarenko.bot",
        "org.telegram.telegrambots"
})
public class TelegramBotApplication {

  public static void main(String[] args) {
    SpringApplication.run(TelegramBotApplication.class, args);
  }

  @Bean
  public static RestTemplateBuilder restTemplateBuilder() {
    return new RestTemplateBuilder();
  }

  @Bean
  public static ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
