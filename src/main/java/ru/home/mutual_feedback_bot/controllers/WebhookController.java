package ru.home.mutual_feedback_bot.controllers;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.home.mutual_feedback_bot.MutualFeedbackBot;

import java.util.concurrent.CompletableFuture;

@RestController
public class WebhookController {
    private final MutualFeedbackBot telegramBot;

    public WebhookController(MutualFeedbackBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public BotApiMethod<Message> onUpdateReceived(@RequestBody Update update) {
//        telegramBot.execute(new SendMessage("527302283", "12345"));
        return telegramBot.onWebhookUpdateReceived(update);
    }
}
