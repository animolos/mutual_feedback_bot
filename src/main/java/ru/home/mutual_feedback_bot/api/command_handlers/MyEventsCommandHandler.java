package ru.home.mutual_feedback_bot.api.command_handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.home.mutual_feedback_bot.entities.Event;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Objects;

@Component
public class MyEventsCommandHandler implements ICommandHandler {

    private final UserService userService;

    public MyEventsCommandHandler(UserService userService) {
        this.userService = userService;
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
            builder.append(String.format("- %s\n", event.getName()));
        }
        String botAnswer = "Your events:\n" + builder;

        return new SendMessage(chatId, botAnswer);
    }
}
