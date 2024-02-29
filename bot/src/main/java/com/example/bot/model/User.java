package com.example.bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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



    @OneToMany(mappedBy = "liked", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<UserLikes> likedBy = new HashSet<>();



    public void setLikedUserId(long chatId) {
        this.likedUserId = chatId;
    }
}
