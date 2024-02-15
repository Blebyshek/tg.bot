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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class MyBot extends TelegramLongPollingBot {

    final BotConfig config;

    private final UserRepository userRepository;




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
    private void sendMessage(long chatId, String textToSend, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(EmojiParser.parseToUnicode(textToSend)); // Поддержка смайликов
        message.setParseMode("HTML");

        // Устанавливаем клавиатуру для сообщения
        message.setReplyMarkup(keyboard);

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
            String messageText = EmojiParser.parseToUnicode(update.getMessage().getText());
            User user = userRepository.findById(chatId).orElse(null);
            Long likedUserId;
            User userLiked;



            if ("/start".equals(messageText)) {
                if (user == null || user.getName()==null || user.getAge()==null || user.getCity()==null || user.getDescription()==null || user.getGender()==null ) {
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
            } else {
                if (user != null) {
                    UserState state = user.getState();

                    // Обработка состояния пользователя
                    switch (state) {
                        case NAME:
                            user.setName(messageText);
                            user.setNickname(update.getMessage().getFrom().getUserName());
                            user.setState(UserState.GENDER);
                            userRepository.save(user);

                            ReplyKeyboardMarkup genderKeyboard = new ReplyKeyboardMarkup();
                            genderKeyboard.setOneTimeKeyboard(true);
                            genderKeyboard.setResizeKeyboard(true);
                            List<KeyboardRow> genderRows=new ArrayList<>();

                            KeyboardRow genderRow = new KeyboardRow();
                            genderRow.add(new KeyboardButton("Мужской"));
                            genderRow.add(new KeyboardButton("Женский"));
                            genderRows.add(genderRow);
                            genderKeyboard.setKeyboard(genderRows);

                            sendMessage(chatId, "Какой у вас пол? (Мужской/Женский)", genderKeyboard);
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
                                case "1", "\uD83D\uDD8A️" -> {
                                    // Редактировать анкету
                                    user.setState(UserState.NAME);
                                    userRepository.save(user);
                                    sendMessage(chatId, "Редактируйте анкету. Как вас зовут?");
                                }
                                case "2", "🔍"-> {
                                    // Смотреть анкеты
                                    user.setCurrentIndex(0);
                                    userRepository.save(user);
                                    showViewKeyboard(chatId);
                                    showOtherUsers(chatId);

                                }
                                case "3", "Моя анкета"-> showProfile(chatId, user);
                                default -> {
                                    sendMessage(chatId, "Некорректная команда. Пожалуйста, выберите команду из меню.");
                                    showMenuKeyboard(chatId);
                                }
                            }
                            break;
                        case VIEW:
                            switch (messageText) {
                                case "like" ->
                                {
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

                                    showViewKeyboard(chatId);
                                    showOtherUsers(chatId);

                                }
                                case "dislike" ->
                                {
                                    showViewKeyboard(chatId);
                                    showOtherUsers(chatId);

                                }
                                case "stop" ->
                                {
                                    showMenuKeyboard(chatId);
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
                            showMenuKeyboard(chatId);
                            user.setState(UserState.MENU);
                            userRepository.save(user);
                        }
                            break;
                        case RECIPROCITY: {
                            switch (messageText){
                                case "Да": {
                                    likedUserId=user.getLikedUserId();
                                    String userLinkHTML;
                                    userLiked = userRepository.findById(likedUserId).orElse(null);
                                    if (userLiked.getNickname()==null){
                                        userLinkHTML = "<a href=\"tg://user?id=" + likedUserId + "\">" + userLiked.getName() + "</a>";
                                        sendMessage(chatId, "Связаться с " + userLinkHTML);
                                    } else {
                                        userLinkHTML = "<a href=\"t.me/" + userLiked.getNickname() + "\">" + userLiked.getName() + "</a>";
                                        sendMessage(chatId, "Связаться с " + userLinkHTML);
                                    }
                                    if (user.getNickname()==null){
                                        userLinkHTML = "<a href=\"tg://user?id=" + chatId + "\">" + user.getName() + "</a>";
                                        sendMessage(likedUserId, "Есть взаимность с " + userLinkHTML);
                                    } else {
                                        userLinkHTML = "<a href=\"t.me/" + user.getNickname() + "\">" + user.getName() + "</a>";
                                        sendMessage(likedUserId, "Есть взаимность  с " + userLinkHTML);
                                    }
                                    //showMenuKeyboard(chatId);
                                    user.setState(UserState.MENU);
                                    userRepository.save(user);
                                }
                                case "Нет": {
                                    showMenuKeyboard(chatId);
                                    user.setState(UserState.MENU);
                                    userRepository.save(user);
                                }

                            }
                        }

                    }
                } else {
                    sendMessage(chatId, "Анкета еще не создана введите /start");

                }
            }
        }
    }
    private void showMenuKeyboard(long chatId) {
        ReplyKeyboardMarkup menuKeyboard = new ReplyKeyboardMarkup();
        menuKeyboard.setOneTimeKeyboard(true);
        menuKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> menuRows =new ArrayList<>();

        KeyboardRow menuRow = new KeyboardRow();
        menuRow.add(new KeyboardButton("\uD83D\uDD8A️"));
        menuRow.add(new KeyboardButton("\uD83D\uDD0D"));
        menuRow.add(new KeyboardButton("Моя анкета"));
        menuRows.add(menuRow);
        menuKeyboard.setKeyboard(menuRows);

        sendMessage(chatId, "Меню:\n1) Редактировать анкету\n2) Смотреть анкеты\n3)Моя анкета", menuKeyboard);
    }
    private void showViewKeyboard(long chatId) {
        ReplyKeyboardMarkup viewKeyboard = new ReplyKeyboardMarkup();
        viewKeyboard.setOneTimeKeyboard(true);
        viewKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> viewRows =new ArrayList<>();

        KeyboardRow viewRow = new KeyboardRow();
        viewRow.add(new KeyboardButton("like"));
        viewRow.add(new KeyboardButton("dislike"));
        viewRow.add(new KeyboardButton("stop"));
        viewRows.add(viewRow);
        viewKeyboard.setKeyboard(viewRows);

        sendMessage(chatId, "\uD83D\uDE09\uD83D\uDDFF", viewKeyboard);
    }
    private void showOtherUsers(long chatId) {
        List<User> userList = userRepository.findAll(); // Извлекает всех пользователей из репозитория.
        User currentUser = userRepository.findById(chatId).orElse(null);
        int currentUserIndex = currentUser.getCurrentIndex();

        userList.removeIf(user -> user.getChatId().equals(chatId));
        userList.sort(Comparator.comparingLong(User::getChatId)); //пользователи в БД менялись местами и был баг показа 1 анкеты, это его убирает

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
            showMenuKeyboard(chatId);
            currentUserIndex = 0;
            currentUser.setCurrentIndex(currentUserIndex);
            currentUser.setState(UserState.MENU);
            userRepository.save(currentUser);
        }


    }


    private void showProfile(long chatId, User user) {

        sendMessage(chatId, "Ваша анкета:\nИмя: " + user.getName() + "\nПол: " + user.getGender() + "\nВозраст: " + user.getAge() + "\nГород: " + user.getCity() + "\nОписание: " + user.getDescription());
        showMenuKeyboard(chatId);
    }


}
