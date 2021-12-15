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
import java.util.Objects;

@Component
public class FeedbackCommandHandler implements ICommandHandler {

    private final String telegramBotUrl;
    private final UserService userService;

    public FeedbackCommandHandler(
            UserService userService,
            BotConfig config) {
        this.userService = userService;
        this.telegramBotUrl = config.getTelegramBotUrl();
    }

    @Override
    public boolean accept(String command) {
        return Objects.equals(command, "/feedback");
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
            if (event.getFeedbacks().isEmpty()) {
                continue;
            }

            builder.append(String.format("Event: %s\n", event.getName()));
            for (Feedback feedback : event.getFeedbacks()
                    .stream()
                    .sorted(Comparator.comparing(Feedback::getCreatedAt))
                    .toList()) {
                if (feedback.isReply()) {
                    continue;
                }
                String params = String.format("%s %s %s", "leaveFeedback", event.getId(), feedback.getId());
                builder.append(String.format("(%s) -> %s\n",
                        String.format("<a href=\"%s\">%s</a>", telegramBotUrl + params, "reply"),
                        feedback.getMessage()));
            }
        }

        String botAnswer = builder.isEmpty()
                ? "Can't find feedback of your events."
                : builder.toString();

        var sendMessage = new SendMessage(chatId, botAnswer);
        sendMessage.setParseMode(ParseMode.HTML);
        return sendMessage;
    }
}
