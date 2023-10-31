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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class MyBot extends TelegramLongPollingBot {

    final BotConfig config;

<<<<<<< Updated upstream
    private UserRepository userRepository;
=======
    private final UserRepository userRepository;



>>>>>>> Stashed changes

    @Autowired
    public MyBot(BotConfig config, UserRepository userRepository) {
        super("6054034146:AAHgKCkrZFpj5V4xlouy2EbVPaYhtTGZo_0");
        this.config = config;
        this.userRepository = userRepository;


    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }



    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(EmojiParser.parseToUnicode(textToSend)); //поддержка смайликов
       message.setParseMode("HTML");
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
            Long likedUserId;
            User userLiked;



            switch (messageText) {

                case "/start" -> {
                    if (user == null) {
                        // Создаем новую анкету
                        User newUser = new User();
                        newUser.setChatId(chatId);
                        newUser.setState(UserState.NAME);
                        userRepository.save(newUser);

<<<<<<< Updated upstream
                        sendMessage(chatId, "Привет! Давай заполним анкету. Как тебя зовут?");
                    } else {
                        user.setState(UserState.MENU); // Устанавливаем состояние в MENU
                        userRepository.save(user);
                        sendMessage(chatId, "У вас уже есть анкета!");
                        showProfile(chatId, user);
=======
                    // Обработка состояния пользователя
                    switch (state) {
                        case NAME:
                            user.setName(messageText);
                            user.setNickname(update.getMessage().getFrom().getUserName());
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
                                case "1" -> {
                                    // Редактировать анкету
                                    user.setState(UserState.NAME);
                                    userRepository.save(user);
                                    sendMessage(chatId, "Редактируйте анкету. Как вас зовут?");
                                }
                                case "2" -> {
                                    // Смотреть анкеты
                                    user.setCurrentIndex(0);
                                    userRepository.save(user);
                                    showOtherUsers(chatId);
                                }
                                case "3" -> showProfile(chatId, user);
                                default -> {
                                    sendMessage(chatId, "Некорректная команда. Пожалуйста, выберите команду из меню.");
                                    sendMessage(chatId, "Меню:\n1) Редактировать анкету\n2) Смотреть анкеты\n3)Моя анкета");
                                }
                            }
                            break;
                        case VIEW:
                            switch (messageText) {
                                case "like" -> {
                                    likedUserId=user.getViewedUserId();
                                    userLiked = userRepository.findById(likedUserId).orElse(null);
                                    sendMessage(chatId, "Лайк отправлен, ждем ответа");
                                    if (userLiked != null) {
                                        userLiked.setState(UserState.LIKED);
                                        userLiked.setLikedUserId(chatId); // Устанавливаем chatId текущего пользователя
                                        userRepository.save(userLiked);
                                        sendMessage(user.getViewedUserId(), "Ваша анкета кому-то понравилась. Хотите посмотреть? (Да/Нет)");
                                    }
                                    // String userLinkHTML = "<a href=\"tg://user?id="+userLikedChatId+"\">inline mention of a user</a>";

                                    showOtherUsers(chatId);
                                }
                                case "dislike" -> showOtherUsers(chatId);
                                case "stop" -> {
                                    user.setState(UserState.MENU);
                                    userRepository.save(user);
                                }
                                default -> sendMessage(chatId, "Некорректная команда.");
                            }
                            break;
                        case LIKED:
                            likedUserId=user.getLikedUserId();
                            userLiked = userRepository.findById(likedUserId).orElse(null);
                            if (messageText.equals("Да"))
                            {
                                if (userLiked != null)
                                {
                                    String message = "Анкета пользователя " + ":\n" +
                                            "Имя: " + userLiked.getName() + "\n"
                                            + "Пол: " + userLiked.getGender() + "\n"
                                            + "Город: " + userLiked.getCity() + "\n"
                                            + "Описание: " + userLiked.getDescription() + "\n\n";
                                    sendMessage(chatId, message);
                                    sendMessage(chatId, "Поставить взаимность? (Да/Нет) ");
                                    user.setState(UserState.RECIPROCITY);
                                    userRepository.save(user);
                                }
                            } if (messageText.equals("Нет"))
                            {
                            user.setState(UserState.MENU);
                            userRepository.save(user);
                             }
                            break;
                        case RECIPROCITY: {
                            switch (messageText){
                                case "Да": {
                                    likedUserId=user.getLikedUserId();
                                    userLiked = userRepository.findById(likedUserId).orElse(null);
                                    if (userLiked.getNickname()==null){
                                        String userLinkHTML1 = "<a href=\"tg://user?id=" + likedUserId + "\">" + userLiked.getName() + "</a>";
                                        sendMessage(chatId, "Связаться с " + userLinkHTML1);
                                        user.setState(UserState.MENU);
                                        userRepository.save(user);
                                    } else {
                                        String userLinkHTML1 = "<a href=\"t.me/" + userLiked.getNickname() + "\">" + userLiked.getName() + "</a>";
                                        sendMessage(chatId, "Связаться с " + userLinkHTML1);
                                        user.setState(UserState.MENU);
                                        userRepository.save(user);
                                    }


                                }
                                case "Нет": {
                                    user.setState(UserState.MENU);
                                    userRepository.save(user);
                                }

                            }
                        }

>>>>>>> Stashed changes
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
                                    case "3":
                                        showProfile(chatId, user);
                                        break;
                                    default:
                                        sendMessage(chatId, "Некорректная команда. Пожалуйста, выберите команду из меню.");
                                        sendMessage(chatId, "Меню:\n1) Редактировать анкету\n2) Смотреть анкеты\n3)Моя анкета");
                                        break;
                                }
                                break;
                            case VIEW:
                                switch (messageText) {
                                    case "like":

                                        sendMessage(chatId, "Лайк отправлен ждем ответа");
                                        showOtherUsers(chatId);

                                        break;
                                    case "dislike":
                                        showOtherUsers(chatId);

                                        break;
                                    case "stop":
                                        user.setState(UserState.MENU);
                                        userRepository.save(user);
                                        break;
                                    default:
                                        sendMessage(chatId, "Некорректная команда.");

                                        break;
                                }

                                break;
                        }
                    } else {
                        sendMessage(chatId,"Анкета еще не создана введите /start");

                    }


                }
            }
        }
    }
<<<<<<< Updated upstream

    private void showOtherUsers(long chatId) {
        List<User> otherUsers = (List<User>) userRepository.findAll();; //извлекает всех пользователей из репозитория.

        User user = userRepository.findById(chatId).orElse(null);

        int currentIndex = 0;

        if (user.getState() == UserState.VIEW) {
            currentIndex = user.getCurrentIndex();
        } else {
            // Новое состояние пользователя
            user.setState(UserState.VIEW);
            userRepository.save(user);

        }

        int totalUsers = otherUsers.size();
        User nextUser = otherUsers.get(currentIndex);
        if (currentIndex < totalUsers && !nextUser.getChatId().equals(chatId)) {


            StringBuilder message = new StringBuilder();
            message.append("Анкета пользователя ").append(":\n");
            message.append("Имя: ").append(nextUser.getName()).append("\n");
            message.append("Пол: ").append(nextUser.getGender()).append("\n");
            message.append("Город: ").append(nextUser.getCity()).append("\n");
            message.append("Описание: ").append(nextUser.getDescription()).append("\n\n");

            // Отправляем анкету пользователю
            sendMessage(chatId, message.toString());

            // Увеличиваем индекс анкеты пользователя
            currentIndex = currentIndex + 1;
            user.setCurrentIndex(currentIndex);
            user.setState(UserState.VIEW);
            userRepository.save(user);
        } else {
            // Все анкеты просмотрены
            sendMessage(chatId, "Все анкеты просмотрены.");
            user.setState(UserState.MENU);
            userRepository.save(user);
        }
=======
        private void showOtherUsers(long chatId) {
        List<User> userList = userRepository.findAll(); // Извлекает всех пользователей из репозитория.
        User currentUser = userRepository.findById(chatId).orElse(null);
        int currentUserIndex = currentUser.getCurrentIndex();

        userList.removeIf(user -> user.getChatId().equals(chatId));
        Collections.sort(userList, Comparator.comparingLong(User::getChatId)); //пользователи в БД менялись местами и был баг показа 1 анкеты, это его убирает

        if (currentUserIndex < userList.size()) {
            User viewedUser = userList.get(currentUserIndex);

            String message = "Анкета пользователя " + viewedUser.getName() + ":\n" +
                    "Имя: " + viewedUser.getName() + "\n" +
                    "Пол: " + viewedUser.getGender() + "\n" +
                    "Город: " + viewedUser.getCity() + "\n" +
                    "Описание: " + viewedUser.getDescription() + "\n\n";

            // Отправляем анкету пользователю
            sendMessage(chatId, message);
            currentUser.setViewedUserId(viewedUser.getChatId());
            currentUserIndex++;
            currentUser.setCurrentIndex(currentUserIndex);
            currentUser.setState(UserState.VIEW);
            userRepository.save(currentUser);
        } else {
            // Если больше пользователей нет, вы можете отправить сообщение о завершении списка
            sendMessage(chatId, "Список пользователей завершен.");
            currentUserIndex = 0;
            currentUser.setCurrentIndex(currentUserIndex);
            currentUser.setState(UserState.MENU);
            userRepository.save(currentUser);
        }


>>>>>>> Stashed changes
    }


    private void showProfile(long chatId, User user) {

        sendMessage(chatId, "Ваша анкета:\nИмя: " + user.getName() + "\nПол: " + user.getGender() + "\nВозраст: " + user.getAge() + "\nГород: " + user.getCity() + "\nОписание: " + user.getDescription());
        sendMessage(chatId, "Меню:\n1) Редактировать анкету\n2) Смотреть анкеты\n3)Моя анкета");
    }




}
