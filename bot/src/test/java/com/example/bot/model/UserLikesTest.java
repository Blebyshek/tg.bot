package com.example.bot.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserLikesTest {

    @Test
    void setBooleanLiker() {
        // Создаем объект UserLikes
        UserLikes userLikes = new UserLikes();

        // Устанавливаем значение booleanLiker
        userLikes.setBooleanLiker(true);

        // Проверяем, что значение booleanLiker установлено правильно
        assertTrue(userLikes.isBooleanLiker());
    }

    @Test
    void setBooleanLiked() {
        // Создаем объект UserLikes
        UserLikes userLikes = new UserLikes();

        // Устанавливаем значение booleanLiked
        userLikes.setBooleanLiked(true);

        // Проверяем, что значение booleanLiked установлено правильно
        assertTrue(userLikes.isBooleanLiked());
    }
}
