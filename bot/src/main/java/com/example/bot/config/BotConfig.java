package com.example.bot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data //автоматически создает конструкторы
@PropertySource("application.properties") // откуда берутся данные
public class BotConfig {

    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String token;
}
