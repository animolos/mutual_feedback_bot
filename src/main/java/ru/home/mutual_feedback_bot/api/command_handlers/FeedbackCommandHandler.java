package ru.home.mutual_feedback_bot.api.command_handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.home.mutual_feedback_bot.config.BotConfig;
import ru.home.mutual_feedback_bot.entities.Event;
import ru.home.mutual_feedback_bot.entities.Feedback;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.services.FeedbackService;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Comparator;
import java.util.Objects;

@Component
public class FeedbackCommandHandler implements ICommandHandler {

    private final String telegramBotUrl;
    private final UserService userService;
    private final FeedbackService feedbackService;

    public FeedbackCommandHandler(
            UserService userService,
            FeedbackService feedbackService,
            BotConfig config) {
        this.userService = userService;
        this.feedbackService = feedbackService;
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

            StringBuilder tempBuilder = new StringBuilder();
            for (Feedback feedback : event.getFeedbacks()
                    .stream()
                    .filter(f -> !f.isRead())
                    .sorted(Comparator.comparing(Feedback::getCreatedAt))
                    .toList()) {
                if (feedback.isReply()) {
                    continue;
                }
                String params = String.format("%s__%s__%s", "leaveFeedback", event.getId(), feedback.getId());
                tempBuilder.append(String.format("(%s) -> %s\n",
                        String.format("<a href=\"%s\">%s</a>", telegramBotUrl + params, "reply"),
                        feedback.getMessage()));

                feedback.setRead(true);
                feedbackService.insertOrUpdate(feedback);
            }

            if (tempBuilder.isEmpty())
                continue;

            builder.append(String.format("Event: %s\n", event.getName()));
            builder.append(tempBuilder);
        }

        String botAnswer = builder.isEmpty()
                ? "No new feedback"
                : builder.toString();

        var sendMessage = new SendMessage(chatId, botAnswer);
        sendMessage.setParseMode(ParseMode.HTML);
        return sendMessage;
    }
}
