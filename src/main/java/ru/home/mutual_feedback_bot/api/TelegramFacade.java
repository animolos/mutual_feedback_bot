package ru.home.mutual_feedback_bot.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.home.mutual_feedback_bot.api.command_handlers.ICommandHandler;
import ru.home.mutual_feedback_bot.api.status_handlers.IStatusHandler;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.models.ConversationStatus;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Arrays;

import static ru.home.mutual_feedback_bot.extensions.Strings.getUnknownCommandMessage;

@Slf4j
@Component
public class TelegramFacade {

    private final UserService userService;
    private final ICommandHandler[] commandHandlers;
    private final IStatusHandler[] statusHandlers;

    public TelegramFacade(
            UserService userService,
            ICommandHandler[] commandHandlers,
            IStatusHandler[] statusHandlers) {
        this.userService = userService;
        this.commandHandlers = commandHandlers;
        this.statusHandlers = statusHandlers;
    }

    public SendMessage handleUpdate(Update update) {
        if (update.hasMessage()) {
            return handleInputMessage(update.getMessage());
        }
        return null;
    }

    private SendMessage handleInputMessage(Message message) {
        User user = getUser(message.getChatId());

        if (!message.hasText()) {
            return null;
        }

        SendMessage sendMessage = tryHandleCommand(message, user);

        if (sendMessage == null) {
            sendMessage = tryHandleStatus(message, user);
        }

        return sendMessage != null ? sendMessage : new SendMessage(user.getId().toString(), getUnknownCommandMessage());
    }

    private SendMessage tryHandleCommand(Message message, User user) {
        String[] tokens = message.getText().trim().split(" ", 2);
        String command = tokens[0];
        String[] args = tokens.length == 2 ? tokens[1].split("__") : new String[0];

        ICommandHandler commandHandler = Arrays.stream(commandHandlers)
                .filter(h -> h.accept(command))
                .findFirst()
                .orElse(null);

        return commandHandler != null ? commandHandler.handle(user, args) : null;
    }
    
    private SendMessage tryHandleStatus(Message message, User user) {
        ConversationStatus status = user.getConversationStatus();
        IStatusHandler statusHandler = Arrays.stream(statusHandlers)
                .filter(h -> h.accept(status))
                .findFirst()
                .orElse(null);

        return statusHandler != null ? statusHandler.handle(message, user) : null;
    }
    
    private User getUser(Long chatId) {
        User user = userService.findById(chatId);

        if (user == null) {
            User userToCreate = new User(chatId);
            user = userService.insertOrUpdate(userToCreate);
        }

        return user;
    }
}
