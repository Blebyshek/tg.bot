package com.example.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

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
<<<<<<< Updated upstream






=======
    private Long likedUserId;
    private Long viewedUserId;


    public void setLikedUserId(long chatId) {
        this.likedUserId = chatId;
    }
>>>>>>> Stashed changes
}
