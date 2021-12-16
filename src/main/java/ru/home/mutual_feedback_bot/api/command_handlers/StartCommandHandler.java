package ru.home.mutual_feedback_bot.api.command_handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.home.mutual_feedback_bot.config.BotConfig;
import ru.home.mutual_feedback_bot.entities.Event;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.models.ConversationStatus;
import ru.home.mutual_feedback_bot.services.EventService;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Collections;
import java.util.Objects;

import static ru.home.mutual_feedback_bot.extensions.Strings.getHelpMessage;

@Component
public class StartCommandHandler implements ICommandHandler {

    private final UserService userService;
    private final EventService eventService;
    private final String telegramBotUrl;

    public StartCommandHandler(
            UserService userService,
            EventService eventService,
            BotConfig config) {
        this.userService = userService;
        this.eventService = eventService;
        this.telegramBotUrl = config.getTelegramBotUrl();
    }

    @Override
    public boolean accept(String command) {
        return Objects.equals(command, "/start");
    }

    @Override
    public SendMessage handle(User user, String[] args) {
        String chatId = user.getId().toString();

        if (args == null || args.length == 0) {
            userService.resetUser(user);
            return new SendMessage(chatId, getHelpMessage());
        }

        SendMessage sendMessage = new SendMessage(chatId, "Something went wrong!");

        if (args[0].equals("leaveFeedback")) {
            Long eventId = Long.parseLong(args[1]);
            Long feedbackId = args.length == 3 ? Long.parseLong(args[2]) : null;
            user.setConversationStatus(ConversationStatus.LeaveFeedback);
            user.setSelectedEventId(eventId);
            user.setSelectedFeedbackId(feedbackId);
            userService.insertOrUpdate(user);
            sendMessage.setText("Write your message");
        }
        else if (args[0].equals("getEventLink")) {
            Long eventId = Long.parseLong(args[1]);
            Event event = eventService.findById(eventId);
            sendMessage.setText(String.format("Event name: %s\nDescription: %s", event.getName(), event.getDescription()));
            InlineKeyboardButton button = new InlineKeyboardButton("Send feedback");
            button.setUrl(telegramBotUrl + "leaveFeedback__" + event.getId());
            InlineKeyboardMarkup markup =
                    new InlineKeyboardMarkup(Collections.singletonList(Collections.singletonList(button)));
            sendMessage.setReplyMarkup(markup);
        }

        return sendMessage;
    }
}
