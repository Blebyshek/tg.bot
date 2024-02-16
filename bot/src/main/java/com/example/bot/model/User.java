package com.example.bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity(name="usersDataTable")
@Getter
@Setter
public class User {
    @Id
    private Long chatId;
    private String nickname;

    private String name;
    private String purpose;
    private Integer age;
    private String faculty;

    private String description;
    private UserState state; // поле для хранения состояния анкеты
    private Integer currentIndex;
    private Long likedUserId;
    private Long viewedUserId;



    public void setLikedUserId(long chatId) {
        this.likedUserId = chatId;
    }
}
