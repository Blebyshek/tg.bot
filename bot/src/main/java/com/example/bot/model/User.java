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
    private String gender;
    private String name;
    private Integer age;
    private String city;
    private String description;
    private UserState state; // поле для хранения состояния анкеты
    private Integer currentIndex;
    private Long likedUserId;
    private Long viewedUserId;

    @ElementCollection
    @CollectionTable(name = "liked_users", joinColumns = @JoinColumn(name = "chat_id"))
    private List<Long> likedUserList;

    public void setLikedUserId(long chatId) {
        this.likedUserId = chatId;
    }
}
