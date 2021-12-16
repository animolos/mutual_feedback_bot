package ru.home.mutual_feedback_bot.api.command_handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.home.mutual_feedback_bot.config.BotConfig;
import ru.home.mutual_feedback_bot.entities.Event;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Objects;

@Component
public class MyEventsCommandHandler implements ICommandHandler {

    private final UserService userService;
    private final String telegramBotUrl;

    public MyEventsCommandHandler(
            UserService userService,
            BotConfig config) {
        this.userService = userService;
        this.telegramBotUrl = config.getTelegramBotUrl();
    }

    @Override
    public boolean accept(String command) {
        return Objects.equals(command, "/my_events");
    }

    @Override
    public SendMessage handle(User user, String[] args) {
        String chatId = user.getId().toString();
        userService.resetUser(user);

        if (user.getEvents().isEmpty()) {
            String botAnswer = "You didn't create any event yet!\nPlease, write /create_event";
            return new SendMessage(chatId, botAnswer);
        }

        StringBuilder builder = new StringBuilder();
        for (Event event: user.getEvents()) {
            String params = String.format("%s__%s", "getEventLink", event.getId());
            builder.append(String.format(
                    "(<a href=\"%s\">get link</a>) -> %s\n", telegramBotUrl + params, event.getName()));
        }
        String botAnswer = "Your events:\n" + builder;

        SendMessage sendMessage = new SendMessage(chatId, botAnswer);
        sendMessage.setParseMode(ParseMode.HTML);
        return sendMessage;
    }
}
