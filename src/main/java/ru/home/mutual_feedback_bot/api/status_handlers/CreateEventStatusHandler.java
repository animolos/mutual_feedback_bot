package ru.home.mutual_feedback_bot.api.status_handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.home.mutual_feedback_bot.config.BotConfig;
import ru.home.mutual_feedback_bot.entities.Event;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.models.ConversationStatus;
import ru.home.mutual_feedback_bot.services.EventService;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Collections;

@Component
public class CreateEventStatusHandler implements IStatusHandler {

    private final UserService userService;
    private final EventService eventService;
    private final String telegramBotUrl;

    public CreateEventStatusHandler(
            UserService userService,
            EventService eventService,
            BotConfig config) {
        this.userService = userService;
        this.eventService = eventService;
        this.telegramBotUrl = config.getTelegramBotUrl();
    }

    @Override
    public boolean accept(ConversationStatus status) {
        return status == ConversationStatus.CreateEvent;
    }

    @Override
    public SendMessage handle(Message message, User user) {
        String chatId = user.getId().toString();
        String[] parts = message.getText().split(" - ", 2);

        userService.resetUser(user);

        if (parts.length != 2) {
            String botAnswer = "Incorrect format!";
            return new SendMessage(chatId, botAnswer);
        }

        String eventName = parts[0];
        String eventDescription = parts[1];
        Event event = new Event(eventName, eventDescription, user);
        event = eventService.createEvent(event);

        String botAnswer = String.format("Event name: %s\nDescription: %s", eventName, eventDescription);

        InlineKeyboardButton button = new InlineKeyboardButton("Send feedback");
        button.setUrl(telegramBotUrl + "leaveFeedback__" + event.getId());

        InlineKeyboardMarkup markup =
                new InlineKeyboardMarkup(Collections.singletonList(Collections.singletonList(button)));

        SendMessage sendMessage = new SendMessage(chatId, botAnswer);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }
}
