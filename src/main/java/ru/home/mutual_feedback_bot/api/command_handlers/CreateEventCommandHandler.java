package ru.home.mutual_feedback_bot.api.command_handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.models.ConversationStatus;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Objects;

@Component
public class CreateEventCommandHandler implements ICommandHandler {

    private final UserService userService;

    public CreateEventCommandHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean accept(String command) {
        return Objects.equals(command, "/create_event");
    }

    @Override
    public SendMessage handle(User user, String[] args) {
        String text = "Please, write in following format:\nevent name - event description";
        user.setConversationStatus(ConversationStatus.CreateEvent);
        userService.insertOrUpdate(user);
        return new SendMessage(user.getId().toString(), text);
    }
}
