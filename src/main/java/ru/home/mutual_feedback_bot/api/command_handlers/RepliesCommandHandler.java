package ru.home.mutual_feedback_bot.api.command_handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.home.mutual_feedback_bot.config.BotConfig;
import ru.home.mutual_feedback_bot.entities.Event;
import ru.home.mutual_feedback_bot.entities.Feedback;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class RepliesCommandHandler implements ICommandHandler {

    private final String telegramBotUrl;
    private final UserService userService;

    public RepliesCommandHandler(
            UserService userService,
            BotConfig config) {
        this.userService = userService;
        this.telegramBotUrl = config.getTelegramBotUrl();
    }

    @Override
    public boolean accept(String command) {
        return Objects.equals(command, "/replies");
    }

    @Override
    public SendMessage handle(User user, String[] args) {
        String chatId = user.getId().toString();
        userService.resetUser(user);

        if (user.getFeedbacks().isEmpty()) {
            String botAnswer = "You didn't leave any feedback yet!";
            return new SendMessage(chatId, botAnswer);
        }

        StringBuilder builder = new StringBuilder();
        for (Event event : user.getEvents()) {
            StringBuilder tempBuilder = new StringBuilder();
            for (Feedback feedback : event.getFeedbacks()
                    .stream()
                    .sorted(Comparator.comparing(Feedback::getCreatedAt))
                    .toList()) {
                List<Feedback> replies = feedback.getChildFeedback().stream()
                        .sorted(Comparator.comparing(Feedback::getCreatedAt)).toList();

                for (Feedback reply : replies) {
                    tempBuilder.append("Reply:\n");
                    tempBuilder.append(String.format("%s\n", reply.getMessage()));
                    tempBuilder.append("On your message:\n");
                    tempBuilder.append(String.format("%s\n", feedback.getMessage()));
                    String params = String.format("%s %s %s", "leaveFeedback", event.getId(), reply.getId());
                    tempBuilder.append(String.format("(<a href=\"%s\">reply</a>)", telegramBotUrl + params));
                    tempBuilder.append("\n-----\n");
                }
            }

            if (!tempBuilder.isEmpty()) {
                builder.append(String.format("Event: %s\n", event.getName()));
                builder.append(tempBuilder);
            }
        }

        String botAnswer = builder.isEmpty()
                ? "No replies to your feedback"
                : builder.toString();

        var sendMessage = new SendMessage(chatId, botAnswer);
        sendMessage.setParseMode(ParseMode.HTML);

        return sendMessage;
    }
}
