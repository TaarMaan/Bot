package com.example.bot.service;

import com.example.bot.config.BotConfig;
import com.example.bot.model.Repository;
import com.example.bot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private Repository repository;
    final BotConfig botConfig;

static final String HELP_TEXT = "This bot is created to demonstrate.\n\n" +
        "You can eecute commands from the main menu on the left or bytyping a comman:\n\n" +
        "Type /start to see a welcome message\n\n" +
        "Type /data to see data stored about yourself\n\n"+
        "Type /help to see this message again";

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        //Этот способ устарел, меню сделано при помощи функционала @BotFathrer
        /*List listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start","Get a welcome message"));
        listofCommands.add(new BotCommand("/myData","Get your data stored"));
        listofCommands.add(new BotCommand("/deleteData","Delete my data"));
        listofCommands.add(new BotCommand("/help","How to use this bot"));
        listofCommands.add(new BotCommand("/settings","Set your preferences"));
        try {
            this.execute(new SetMyCommands(listofCommands,new BotCommandScopeDefault(),null));
        }
        catch (TelegramApiException e){
            log.error("error setting bot's comman list: " + e.getMessage());
        }*/
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start":

                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "sorry, this command is unknown for bot");
                    break;
            }
        }
    }

    private void registerUser(Message message) {
        if(repository.findById(message.getChatId()).isEmpty()){
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setRegisterAt(new Timestamp(System.currentTimeMillis()));

            repository.save(user);
            log.info("user saved: " + user);
        }

    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you";
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}
