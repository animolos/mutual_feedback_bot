package ru.home.mutual_feedback_bot;

import javafx.util.Pair;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.home.mutual_feedback_bot.api.TelegramFacade;

public class MutualFeedbackBot extends TelegramWebhookBot {
    private String webHookPath;
    private String botUserName;
    private String botToken;

    private final TelegramFacade telegramFacade;

    public MutualFeedbackBot(DefaultBotOptions botOptions, TelegramFacade telegramFacade) {
        super(botOptions);
        this.telegramFacade = telegramFacade;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotPath() {
        return webHookPath;
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        Pair<SendMessage, SendMessage> res = telegramFacade.handleUpdate(update);
//        if (res.getValue() != null) {
//            try {
//                sendApiMethod(res.getValue()); // TODO: 17/11/2021 как-то послать сообщение другому пользователю
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
//        }
//
        try {
            execute(res.getKey());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
//        execute(res.getKey());
        return res.getKey();
    }

    public void setWebHookPath(String webHookPath) {
        this.webHookPath = webHookPath;
    }

    public void setBotUserName(String botUserName) {
        this.botUserName = botUserName;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }
}
