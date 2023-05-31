package com.example.bot;

import com.example.bot.config.BotConfig;
import com.example.bot.model.User;
import com.example.bot.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyBot extends TelegramLongPollingBot {

    final BotConfig config;
    @Autowired
    private UserRepository userRepository;

    public MyBot (BotConfig config) {
        this.config = config;

    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){   // Проверка пришло что-то и есть текст
            String messageText=update.getMessage().getText();   //Получение сообщения
            long chatId=update.getMessage().getChatId(); // Чат Ид

            switch (messageText) {
                case "/start" -> {
                    registerUser(update);
                    sendMessage(chatId, "Привет Абобус");
                }

                case "/help" -> sendMessage(chatId, "This commadadadadadnd is not supported, type /help for more info11111 ");

                default -> sendMessage(chatId, "This comm222222nfo111122221 ");
            }
        }


    }

    private void sendMessage (long chatId, String textToSend){
        SendMessage message=new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);


        try {
            execute(message);
        } catch (TelegramApiException e){

        }
    }

    private void registerUser(Update update) {
        String msg=update.getMessage().getText();
        long chatId=update.getMessage().getChatId();
        if (userRepository.findById(chatId).isEmpty()) {

            User user = new User();
            user.setChatId(chatId);
            sendMessage(chatId, "Добро пожаловать! Пожалуйста, заполните анкету.");
            sendMessage(chatId, "Введите ваш пол:");
            user.setGender(msg);
            sendMessage(chatId, "Введите ваш возраст:");
            user.setAge(Integer.parseInt(msg));
            sendMessage(chatId, "Введите ваш город:");
            user.setCity(msg);
            sendMessage(chatId, "Введите имя:");
            user.setName(msg);
            sendMessage(chatId, "Введите описание:");
            user.setDescription(msg);
            userRepository.save(user);




        }
    }


}
