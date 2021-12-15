package ru.home.mutual_feedback_bot.api.command_handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.models.ConversationStatus;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Objects;

import static ru.home.mutual_feedback_bot.extensions.Strings.getHelpMessage;

@Component
public class StartCommandHandler implements ICommandHandler {

    private final UserService userService;

    public StartCommandHandler(UserService userService) {
        this.userService = userService;
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

        String botAnswer;

        if (args[0].equals("leaveFeedback")) {
            Long eventId = Long.parseLong(args[1]);
            Long feedbackId = args.length == 3 ? Long.parseLong(args[2]) : null;
            user.setConversationStatus(ConversationStatus.LeaveFeedback);
            user.setSelectedEventId(eventId);
            user.setSelectedFeedbackId(feedbackId);
            userService.insertOrUpdate(user);
            botAnswer = "Write your message";
        }
        else {
            botAnswer = "Something went wrong!";
        }

        return new SendMessage(chatId, botAnswer);
    }
}
