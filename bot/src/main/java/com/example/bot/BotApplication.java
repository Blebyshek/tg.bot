package com.example.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class BotApplication {

	public static void main(String[] args) throws TelegramApiException {
		SpringApplication.run(BotApplication.class, args);
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		MyBot myBot = new MyBot();
		try {
			telegramBotsApi.registerBot(myBot);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

}
