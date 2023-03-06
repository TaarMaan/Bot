package com.example.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@Entity(name = "usersDataTable")
public class User {
    //ID пользователя
    @Id
    private Long chatId;

    //Имя пользователя Telegram
    private String firstName;

    //Время регистрирования в боте (/start)
    private Timestamp registerAt;

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", registerAt=" + registerAt +
                '}';
    }
    public User() {

    }
}
