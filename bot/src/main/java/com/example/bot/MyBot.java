package com.example.bot;

import com.example.bot.config.BotConfig;
import com.example.bot.model.User;
import com.example.bot.model.UserRepository;
import com.example.bot.model.UserState;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class MyBot extends TelegramLongPollingBot {

    final BotConfig config;

    private UserRepository userRepository;

    @Autowired
    public MyBot(BotConfig config, UserRepository userRepository) {
        this.config = config;
        this.userRepository = userRepository;

    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(EmojiParser.parseToUnicode(textToSend)); //поддержка смайликов
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            User user = userRepository.findById(chatId).orElse(null);

            switch (messageText) {

                case "/start" -> {
                    if (user == null) {
                        // Создаем новую анкету
                        User newUser = new User();
                        newUser.setChatId(chatId);
                        newUser.setState(UserState.NAME);
                        userRepository.save(newUser);

                        sendMessage(chatId, "Привет! Давай заполним анкету. Как тебя зовут?");
                    } else {
                        user.setState(UserState.MENU); // Устанавливаем состояние в MENU
                        userRepository.save(user);
                        sendMessage(chatId, "У вас уже есть анкета!");
                        showProfile(chatId, user);
                    }
                    break;
                }
                default -> {

                    if (user != null) {
                        UserState state = user.getState();

                        // Обработка состояния пользователя
                        switch (state) {
                            case NAME:
                                user.setName(messageText);
                                user.setState(UserState.GENDER);
                                userRepository.save(user);
                                sendMessage(chatId, "Какой у вас пол? (Мужской/Женский)");
                                break;
                            case GENDER:
                                if (messageText.equalsIgnoreCase("Мужской") || messageText.equalsIgnoreCase("Женский")) {
                                    user.setGender(messageText);
                                    user.setState(UserState.AGE);
                                    userRepository.save(user);
                                    sendMessage(chatId, "Укажите ваш возраст.");
                                } else {
                                    sendMessage(chatId, "Некорректное значение пола. Пожалуйста, введите 'Мужской' или 'Женский'.");
                                    return;
                                }
                                break;
                            case AGE:
                                try {
                                    int age = Integer.parseInt(messageText);
                                    user.setAge(age);
                                    user.setState(UserState.CITY);
                                    userRepository.save(user);
                                    sendMessage(chatId, "В каком городе вы живете?");
                                } catch (NumberFormatException e) {
                                    sendMessage(chatId, "Некорректное значение возраста. Пожалуйста, введите целое число для возраста.");
                                    return;
                                }
                                break;
                            case CITY:
                                user.setCity(messageText);
                                user.setState(UserState.DESCRIPTION);
                                userRepository.save(user);
                                sendMessage(chatId, "Расскажите немного о себе.");
                                break;
                            case DESCRIPTION:
                                if (messageText.length() <= 100) {
                                    user.setDescription(messageText);
                                    user.setState(UserState.MENU); // Переход в состояние меню после заполнения анкеты
                                    userRepository.save(user);
                                    sendMessage(chatId, "Анкета успешно заполнена!");
                                    showProfile(chatId, user);

                                } else {
                                    sendMessage(chatId, "Максимальная длина описания - 100 символов. Пожалуйста, введите описание, которое не превышает 100 символов.");
                                    sendMessage(chatId, "Расскажите немного о себе.");
                                }
                                break;
                            case COMPLETED:
                                sendMessage(chatId, "Вы уже заполнили анкету.");
                                break;
                            case MENU:
                                switch (messageText) {
                                    case "1":
                                        // Редактировать анкету
                                        user.setState(UserState.NAME);
                                        userRepository.save(user);
                                        sendMessage(chatId, "Редактируйте анкету. Как вас зовут?");
                                        break;
                                    case "2":
                                        // Смотреть анкеты
                                        showOtherUsers(chatId);

                                        break;
                                    default:
                                        sendMessage(chatId, "Некорректная команда. Пожалуйста, выберите команду из меню.");
                                        sendMessage(chatId, "Меню:\n1) Редактировать анкету\n2) Смотреть анкеты");
                                        break;
                                }
                                break;
                            case VIEW:
                                showOtherUsers(chatId);
                                break;
                        }
                    } else {
                        sendMessage(chatId,"Анкета еще не создана введите /start");

                    }


                }
            }

        }


    }

    private void showOtherUsers(long chatId) {
        List<User> otherUsers = (List<User>) userRepository.findAll();; //извлекает всех пользователей из репозитория.

        User user = userRepository.findById(chatId).orElse(null);

        int currentIndex = 0;

        if (user != null && user.getState() == UserState.VIEW) {
            currentIndex = user.getCurrentIndex();
        } else {
            // Новое состояние пользователя
            user.setState(UserState.VIEW);
            userRepository.save(user);

        }

        int totalUsers = otherUsers.size();
        if (currentIndex < totalUsers) {
            User nextUser = otherUsers.get(currentIndex);
            StringBuilder message = new StringBuilder();
            message.append("Анкета пользователя ").append(currentIndex + 1).append(" из ").append(totalUsers).append(":\n");
            message.append("Имя: ").append(nextUser.getName()).append("\n");
            message.append("Пол: ").append(nextUser.getGender()).append("\n");
            message.append("Город: ").append(nextUser.getCity()).append("\n");
            message.append("Описание: ").append(nextUser.getDescription()).append("\n\n");

            // Отправляем анкету пользователю
            sendMessage(chatId, message.toString());

            // Увеличиваем индекс анкеты пользователя
            currentIndex = currentIndex + 1;
            user.setCurrentIndex(currentIndex);
            userRepository.save(user);
        } else {
            // Все анкеты просмотрены
            sendMessage(chatId, "Все анкеты просмотрены.");
            user.setState(UserState.MENU);
            userRepository.save(user);
        }
    }


    private void showProfile(long chatId, User user) {

        sendMessage(chatId, "Ваша анкета:\nИмя: " + user.getName() + "\nПол: " + user.getGender() + "\nВозраст: " + user.getAge() + "\nГород: " + user.getCity() +
                "\nОписание: " + user.getDescription());
        sendMessage(chatId, "Меню:\n1) Редактировать анкету\n2) Смотреть анкеты");
    }




}
