package com.example.bot.service;

import com.example.bot.config.BotConfig;
import com.example.bot.model.Ads;
import com.example.bot.model.Diary;
import com.example.bot.reposytory.DiaryRepository;
//import com.example.bot.reposytory.Favourites;
import com.example.bot.reposytory.UserRepository;
import com.example.bot.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import com.example.bot.reposytory.AdsRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.example.bot.model.fields.Fields;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot{
    //Объекты
    private final SendMessage message = new SendMessage();
    private final EditMessageText editMessageText = new EditMessageText();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdsRepository AdsRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    //@Autowired
    //private Favourites favourites;
    final BotConfig botConfig;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.contains("/send") && botConfig.getOwnerId() == chatId) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepository.findAll();
                for (User user : users) {
                    sendMessage(user.getChatId(), textToSend);
                    sendAds();
                }
            } else {
                //start,data,deletedate,help,settings,add,delete.
                switch (messageText) {
                    case "/start" -> {registerUser(update.getMessage());
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    }
                    case "/help" -> sendMessage(chatId, Fields.HELP_TEXT);
                    //case "/add" -> addCommandReceived(update.getMessage(),chatId,update.getMessage().getChat().getFirstName(),);
                    //case "/delete" -> keyboardSendMessage(chatId);
                    //case "/favourite" -> keyboardSendMessage(chatId);
                    default -> sendMessage(chatId, "Sorry, this command is unknown for bot");
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals(Fields.YES_BUTTON)) {
                String text = "You pressed YES button";
                executeEditMessageText(text, chatId, messageId);
            } else if (callbackData.equals(Fields.NO_BUTTON)) {
                String text = "You pressed NO button";
                executeEditMessageText(text, chatId, messageId);
            }
        }


    }

    private void executeEditMessageText(String text, long chatId, long messageId) {
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setText(text);
        editMessageText.setMessageId((int) messageId);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error(Fields.ERROR_TEXT + e.getMessage());
        }
    }

    private void addCommandReceived(Message message, long chatId, String name, Diary diary) {
        //String text = update.getMessage().getText();
        String[] arr = message.getText().split(" ");
        diary.setNameTitle(arr[0]);
        diary.setCategory(arr[1]);
        diary.setRating(Double.valueOf(arr[2]));
        diary.setDescription(arr[3]);
        //var diar = diaryRepository.
        //var textToSend = EmojiParser.parseToUnicode(text.substring(text.indexOf(" ")));
        //var users = userRepository.findAll();
        //for (User user : users) {
        //   sendMessage(user.getChatId(), textToSend);
        //}
    }

    private void deleteCommandReceived() {
    }

    /**
     * /start
     * registerUser - регистрация нового пользователя в базе пользователей
     * startCommandReceived - Вывод приветственного сообщения
     */
    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setRegisterAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user);
        }

    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you!" + ":blush:" + "Now, the bot has recorded your data" + ":blush:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }


    /**
     * Функции для бота
     * sendMessage - //Метод отправляющий сообщение ботом
     * executeMessage - //Логирование сообщения ошибки
     * sendAds - Логика для задания таймера отправления рекламы /send владельцем
     *
     * @Scheduled(cron = "0 * * * * *") - аннотация, которая выполняется при выполнении
     * и реализуя делай из cron, задает определенный интервал повторения сообщения для всех юзеров
     * меняем * -> 0, если хотим задать данный таймер. Первый знак - секунды, второй - минуты и.т.д.
     */
    private void sendMessage(long chatId, String textToSend) {
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(Fields.ERROR_TEXT + e.getMessage());
        }
    }

    // Не забыть засунуть это в функцию /send
    @Scheduled(cron = "${cron.scheduler}")
    private void sendAds() {
        var ads = AdsRepository.findAll();
        var users = userRepository.findAll();
        for (Ads ad : ads) {
            for (User user : users) {
                sendMessage(user.getChatId(), ad.getAd());
            }
        }
    }

}