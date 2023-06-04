package com.example.bot.model;

public enum UserState {

    NOT_STARTED, // User has not started the questionnaire
    NAME,        // User is entering their name
    GENDER,      // User is selecting their gender
    AGE,         // User is entering their age
    CITY,        // User is entering their city
    DESCRIPTION, // User is entering their description
    COMPLETED

}
