package com.example.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name="usersDataTable")
public class User {
    @Id
    private Long chatId;

    private String gender;
    private String name;
    private Integer age;
    private String city;
    private String description;
    private String state; // поле для хранения состояния анкеты
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
