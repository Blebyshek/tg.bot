package com.example.bot.config;

import com.example.bot.MyBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class BotInitializer {
    @Autowired //автоматическое поключение Спринг
    MyBot bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi=new TelegramBotsApi(DefaultBotSession.class);
        try{
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e){

        }
    }

}
