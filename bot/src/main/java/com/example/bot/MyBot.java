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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        SendMessage message=new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(EmojiParser.parseToUnicode(textToSend)); //поддержка смайликов
        try {
            execute(message);
        } catch (TelegramApiException e){
            e.printStackTrace();
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
            user.setState(UserState.MENU); // Устанавливаем состояние в MENU
            userRepository.save(user);
            sendMessage(chatId, "У вас уже есть анкета!");
            showProfile(chatId, user);
        }
    }

    private void processQuestionnaire(long chatId, String text) {
        User user = userRepository.findById(chatId).orElse(null);

        if (user != null) {
            UserState state = user.getState();

            if (state == UserState.MENU) {
                // Обработка команд из меню
                switch (text) {
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
            } else if (state == UserState.VIEW) {
                showOtherUsers(chatId);
            } else {
                // Обработка заполнения анкеты
                switch (state) {
                    case NAME:
                        user.setName(text);
                        user.setState(UserState.GENDER);
                        userRepository.save(user);
                        sendMessage(chatId, "Какой у вас пол? (Мужской/Женский)");
                        break;
                    case GENDER:
                        if (text.equalsIgnoreCase("Мужской") || text.equalsIgnoreCase("Женский")) {
                            user.setGender(text);
                            user.setState(UserState.AGE);
                            userRepository.save(user);
                            sendMessage(chatId, "Укажите ваш возраст.");
                        } else {
                            sendMessage(chatId, "Некорректное значение пола. Пожалуйста, введите 'Мужской' или 'Женский'.");
                            sendMessage(chatId, "Какой у вас пол? (Мужской/Женский)");
                        }
                        break;
                    case AGE:
                        try {
                            int age = Integer.parseInt(text);
                            user.setAge(age);
                            user.setState(UserState.CITY);
                            userRepository.save(user);
                            sendMessage(chatId, "В каком городе вы живете?");
                        } catch (NumberFormatException e) {
                            sendMessage(chatId, "Некорректное значение возраста. Пожалуйста, введите целое число для возраста.");
                            sendMessage(chatId, "Укажите ваш возраст.");
                        }
                        break;
                    case CITY:
                        user.setCity(text);
                        user.setState(UserState.DESCRIPTION);
                        userRepository.save(user);
                        sendMessage(chatId, "Расскажите немного о себе.");
                        break;
                    case DESCRIPTION:
                        if (text.length() <= 100) {
                            user.setDescription(text);
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
                    default:
                        sendMessage(chatId, "Неправильный шаг анкеты.");
                        break;
                }
            }
        } else {
            sendMessage(chatId, "Для начала запустите бота с командой /start.");
        }
    }


    private void showProfile(long chatId, User user) {

        sendMessage(chatId, "Ваша анкета:\nИмя: " + user.getName() + "\nПол: " + user.getGender() + "\nВозраст: " + user.getAge() + "\nГород: " + user.getCity() +
                "\nОписание: " + user.getDescription());
        sendMessage(chatId, "Меню:\n1) Редактировать анкету\n2) Смотреть анкеты");
    }

    private void showOtherUsers(long chatId) {
        List<User> otherUsers = StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()); //извлекает всех пользователей из репозитория.




        User user = userRepository.findById(chatId).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
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


}
