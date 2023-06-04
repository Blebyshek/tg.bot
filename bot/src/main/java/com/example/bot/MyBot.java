package com.example.bot;

import com.example.bot.config.BotConfig;
import com.example.bot.model.User;
import com.example.bot.model.UserRepository;
import com.example.bot.model.UserState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyBot extends TelegramLongPollingBot {

    final BotConfig config;

    private UserRepository userRepository;

    @Autowired
    public MyBot(BotConfig config, UserRepository userRepository) {
        this.config = config;
        this.userRepository=userRepository;

    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    private void sendMessage(long chatId, Object message) {
        List<String> existingMessages = new ArrayList<>();

        // Обрабатываем аргумент message
        if (message instanceof String) {
            existingMessages.add((String) message);
        } else if (message instanceof List) {
            existingMessages.addAll((List<String>) message);
        }

        // Отправляем все сообщения
        for (String textToSend : existingMessages) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText(textToSend);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }


    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String text = message.getText();

            if ("/start".equals(text)) {
                processStartCommand(chatId);
            } else {
                processQuestionnaire(chatId, text);
            }
        }
    }

    private void processStartCommand(long chatId) {
        User user = userRepository.findById(chatId).orElse(null);

        if (user == null) {
            // Создаем новую анкету
            User newUser = new User();
            newUser.setChatId(chatId);
            newUser.setState(UserState.NAME);
            userRepository.save(newUser);

            sendMessage(chatId, "Привет! Давай заполним анкету. Как тебя зовут?");
        } else {
            sendMessage(chatId, "У вас уже есть анкета!");
        }
    }

    private void processQuestionnaire(long chatId, String text) {
        User user = userRepository.findById(chatId).orElse(null);

        if (user != null) {
            UserState state = user.getState();

            switch (state) {
                case NAME:
                    user.setName(text);
                    user.setState(UserState.GENDER);
                    userRepository.save(user);
                    sendMessage(chatId, "Какой у тебя пол? (Мужской/Женский)");
                    break;
                case GENDER:
                    if (text.equals("Мужской") || text.equals("Женский")) {
                        user.setGender(text);
                        user.setState(UserState.AGE);
                        userRepository.save(user);
                        sendMessage(chatId, "Сколько тебе лет?");
                    } else {
                        sendMessage(chatId, "Некорректное значение пола. Пожалуйста, введите 'Мужской' или 'Женский'.");
                        sendMessage(chatId, "Какой у тебя пол? (Мужской/Женский)");
                    }
                    break;
                case AGE:
                    try {
                        int age = Integer.parseInt(text);
                        user.setAge(age);
                        user.setState(UserState.CITY);
                        userRepository.save(user);
                        sendMessage(chatId, "В каком городе ты живешь?");
                    } catch (NumberFormatException e) {
                        sendMessage(chatId, "Некорректное значение возраста. Пожалуйста, введите целое число для возраста.");
                        sendMessage(chatId, "Сколько тебе лет?");
                    }
                    break;
                case CITY:
                    user.setCity(text);
                    user.setState(UserState.DESCRIPTION);
                    userRepository.save(user);
                    sendMessage(chatId, "Расскажи немного о себе");
                    break;
                case DESCRIPTION:
                    if (text.length() <= 100) {
                        user.setDescription(text);
                        user.setState(UserState.COMPLETED);
                        userRepository.save(user);
                        sendMessage(chatId, "Анкета успешно заполнена!");
                        showProfile(chatId, user);
                    } else {
                        sendMessage(chatId, "Максимальная длина описания - 100 символов. Пожалуйста, введите описание, которое не превышает 100 символов.");
                        sendMessage(chatId, "Расскажи немного о себе");
                    }
                    break;
                case COMPLETED:
                    sendMessage(chatId, "Вы уже заполнили анкету.");
                    break;
                default:
                    sendMessage(chatId, "Неправильный шаг анкеты.");
                    break;
            }
        } else {
            sendMessage(chatId, "Для начала запустите бота с командой /start.");
        }
    }

    private void showProfile(long chatId, User user) {

        sendMessage(chatId, "Ваша анкета:\nИмя: " + user.getName() + "\nПол: " + user.getGender()+"\nВозраст: " + user.getAge()+"\nГород: " + user.getCity()+
"\nОписание: " + user.getDescription());
        sendMessage(chatId,"Меню:\n1) Редактировать анкету\n2) Смотреть анкеты");
    }


}
