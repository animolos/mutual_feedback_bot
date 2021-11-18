package ru.home.mutual_feedback_bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.home.mutual_feedback_bot.api.TelegramFacade;
import ru.home.mutual_feedback_bot.config.BotConfig;

@Component
public class MutualFeedbackBot extends TelegramWebhookBot {

    private final String webHookPath;
    private final String botUserName;
    private final String botToken;

    private final TelegramFacade telegramFacade;

    public MutualFeedbackBot(BotConfig config, DefaultBotOptions botOptions, TelegramFacade telegramFacade) {
        super(botOptions);
        this.webHookPath = config.getWebHookPath();
        this.botUserName = config.getBotUserName();
        this.botToken = config.getBotToken();
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
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return telegramFacade.handleUpdate(update);
    }
}
