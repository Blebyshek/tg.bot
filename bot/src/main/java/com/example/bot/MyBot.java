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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

    private void sendMessage (long chatId, String textToSend){
        SendMessage message=new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);


        try {
            execute(message);
        } catch (TelegramApiException e){

        }
    }
//    @Override
//    public void onUpdateReceived(Update update) {
//        if (update.hasMessage() && update.getMessage().hasText()){   // Проверка пришло что-то и есть текст
//            String messageText=update.getMessage().getText();   //Получение сообщения
//            long chatId=update.getMessage().getChatId(); // Чат Ид
//
//            switch (messageText) {
//                case "/start" -> {
//                    registerUser(update);
//                    sendMessage(chatId, "Привет Абобус");
//                }
//
//                case "/help" -> sendMessage(chatId, "This commadadadadadnd is not supported, type /help for more info11111 ");
//
//                default -> sendMessage(chatId, "This comm222222nfo111122221 ");
//            }
//        }



    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String text = message.getText();

            User user = userRepository.findById(chatId).orElse(null);

            if ("/start".equals(text)) {
                if (user == null) {
                    // Создаем новую анкету
                    User newUser = new User();
                    newUser.setChatId(chatId);
                    newUser.setState("name"); // устанавливаем начальное состояние анкеты
                    userRepository.save(newUser);

                    sendMessage(chatId, "Привет! Давай заполним анкету. Как тебя зовут?");
                } else {
                    sendMessage(chatId, "У вас уже есть анкета!");
                }
            } else {
                if (user != null) {
                    String state = user.getState();

                    // Обработка состояний анкеты
                    switch (state) {
                        case "name":
                            user.setName(text);
                            user.setState("gender");
                            userRepository.save(user);
                            sendMessage(chatId, "Какой у тебя пол? (Мужской/Женский)");
                            break;
                        case "gender":
                            user.setGender(text);
                            user.setState("age");
                            userRepository.save(user);
                            sendMessage(chatId, "Сколько тебе лет?");
                            break;
                        case "age":
                            int age;
                            try {
                                age = Integer.parseInt(text);
                                user.setAge(age);
                                user.setState("city");
                                userRepository.save(user);
                                sendMessage(chatId, "В каком городе ты живешь?");
                            } catch (NumberFormatException e) {
                                sendMessage(chatId, "Некорректное значение возраста. Пожалуйста, введите целое число для возраста.");
                                sendMessage(chatId, "Сколько тебе лет?");
                            }
                            break;

                        case "city":
                            user.setCity(text);
                            user.setState("description");
                            userRepository.save(user);
                            sendMessage(chatId, "Расскажи немного о себе");
                            break;
                        case "description":
                            user.setDescription(text);
                            user.setState("complete");
                            userRepository.save(user);
                            sendMessage(chatId, "Анкета успешно заполнена!");
                            break;
                        default:
                            sendMessage(chatId, "Неправильный шаг анкеты.");
                            break;
                    }
                } else {
                    sendMessage(chatId, "Для начала запустите бота с командой /start.");
                }
            }
        }
    }




//    private void registerUser(Update update) {
//        String msg=update.getMessage().getText();
//        long chatId=update.getMessage().getChatId();
//        if (userRepository.findById(chatId).isEmpty()) {
//
//            User user = new User();
//            user.setChatId(chatId);
//            sendMessage(chatId, "Добро пожаловать! Пожалуйста, заполните анкету.");
//            sendMessage(chatId, "Введите ваш пол:");
//            user.setGender(msg);
//            sendMessage(chatId, "Введите ваш возраст:");
//            user.setAge(Integer.parseInt(msg));
//            sendMessage(chatId, "Введите ваш город:");
//            user.setCity(msg);
//            sendMessage(chatId, "Введите имя:");
//            user.setName(msg);
//            sendMessage(chatId, "Введите описание:");
//            user.setDescription(msg);
//            userRepository.save(user);
//
//
//
//
//        }
//    }


}
