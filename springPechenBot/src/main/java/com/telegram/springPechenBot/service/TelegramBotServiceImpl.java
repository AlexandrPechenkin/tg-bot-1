package com.telegram.springPechenBot.service;

import com.telegram.springPechenBot.config.BotConfig;
import com.telegram.springPechenBot.model.User;
import com.telegram.springPechenBot.repository.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TelegramBotServiceImpl extends TelegramLongPollingBot {

    private final BotConfig config;
    private final UserRepository userRepository;

    public TelegramBotServiceImpl(BotConfig config, UserRepository userRepository) {
        this.config = config;
        this.userRepository = userRepository;

        List<BotCommand> commandsList = new ArrayList<>();
        commandsList.add(new BotCommand("/start", "start bot"));
        commandsList.add(new BotCommand("/stop", "stop bot"));
        commandsList.add(new BotCommand("/pron", "Что бы паша обдрочился"));
        try {
            this.execute(new SetMyCommands(commandsList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    log.info("Отправили ответ на start для пользователя " + update.getMessage().getChat().getFirstName());
                    saveUser(update.getMessage());
                    break;
                case "/pron":
                    drochila(chatId);
                    log.info("Отправили ответ на pron для пользователя " + update.getMessage().getChat().getFirstName());
                    break;
                default:
                    sendMessage(chatId, "Хуйню пишешь. /start напиши");
            }
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = EmojiParser.parseToUnicode("Hi, " + firstName + " :blush:");
        sendMessage(chatId, answer);
        callTimeout(2);
        sendMessage(chatId, "Иди нахуй");
    }



    private void drochila(long chatId) {
        sendMessage(chatId, EmojiParser.parseToUnicode("Иди нахуй, дрочи ебаное" + " :boy|type_6:"));
        callTimeout(1);
        sendMessage(chatId, "https://rt.pornhub.com/view_video.php?viewkey=64c9750abe228");
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = getKeyboardRows();

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(replyKeyboardMarkup);


        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static List<KeyboardRow> getKeyboardRows() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("/weather");
        row.add("/start");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("/pron");
        keyboardRows.add(row);


        row = new KeyboardRow();
        row.add("/pron2");
        keyboardRows.add(row);


        row = new KeyboardRow();
        row.add("/pron3");
        row.add("/pron4");
        row.add("/pron5");
        row.add("/pron6");
        row.add("/pron7");
        row.add("/pron8");
        keyboardRows.add(row);
        return keyboardRows;
    }

    private void callTimeout(long second) {
        try {
            Thread.sleep(second * 1000);
        } catch (Exception e) {
            log.error("Error occurred: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    private void saveUser(Message message) {
        if(!userRepository.existsById(message.getChatId())) {

            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            userRepository.save(user);

            log.info("Saving user: " + user);
        }
    }
}
