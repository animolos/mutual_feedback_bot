package ru.home.mutual_feedback_bot.api.status_handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.home.mutual_feedback_bot.entities.Event;
import ru.home.mutual_feedback_bot.entities.Feedback;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.models.ConversationStatus;
import ru.home.mutual_feedback_bot.services.EventService;
import ru.home.mutual_feedback_bot.services.FeedbackService;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Date;

@Component
public class LeaveFeedbackStatusHandler implements IStatusHandler {

    private final UserService userService;
    private final EventService eventService;
    private final FeedbackService feedbackService;

    public LeaveFeedbackStatusHandler(
            UserService userService,
            EventService eventService,
            FeedbackService feedbackService) {
        this.userService = userService;
        this.eventService = eventService;
        this.feedbackService = feedbackService;
    }

    @Override
    public boolean accept(ConversationStatus status) {
        return status == ConversationStatus.LeaveFeedback;
    }

    @Override
    public SendMessage handle(Message message, User user) {
        String chatId = user.getId().toString();

        Long selectedEventId = user.getSelectedEventId();
        Long selectedFeedbackId = user.getSelectedFeedbackId();
        if (selectedEventId == null) {
            String botAnswer = "Something went wrong!";
            return new SendMessage(chatId, botAnswer);
        }

        Event selectedEvent = eventService.findById(selectedEventId);
        Feedback feedback = new Feedback(message.getText(), selectedEvent, user, new Date());

        if (selectedFeedbackId != null) {
            Feedback parentFeedback = feedbackService.findById(selectedFeedbackId);
            feedback.setParentFeedback(parentFeedback);
        }

        feedbackService.createFeedback(feedback);
        userService.resetUser(user);

        String botAnswer = "Successfully send!";
        return new SendMessage(chatId, botAnswer);
    }
}
