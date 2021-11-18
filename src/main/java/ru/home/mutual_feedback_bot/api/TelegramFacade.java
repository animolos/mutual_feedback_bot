package ru.home.mutual_feedback_bot.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.home.mutual_feedback_bot.config.BotConfig;
import ru.home.mutual_feedback_bot.entities.Event;
import ru.home.mutual_feedback_bot.entities.Feedback;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.models.ConversationStatus;
import ru.home.mutual_feedback_bot.services.EventService;
import ru.home.mutual_feedback_bot.services.FeedbackService;
import ru.home.mutual_feedback_bot.services.UserService;

import java.util.Collections;

@Slf4j
@Component
public class TelegramFacade {

    private final String telegramBotUrl;

    private final UserService userService;
    private final EventService eventService;
    private final FeedbackService feedbackService;

    public TelegramFacade(
            BotConfig config,
            UserService userService,
            EventService eventService,
            FeedbackService feedbackService) {
        this.telegramBotUrl = config.getTelegramBotUrl();
        this.userService = userService;
        this.eventService = eventService;
        this.feedbackService = feedbackService;
    }

    public SendMessage handleUpdate(Update update) {
        if (update.hasMessage()) {
            return handleInputMessage(update.getMessage());
        } else {
            return null;
        }
    }

    private SendMessage handleInputMessage(Message message) {
        Long chatId = message.getChatId();

        User user = userService.findById(chatId);

        if (user == null) {
            User userToCreate = new User(chatId);
            user = userService.insertOrUpdate(userToCreate);
        }

        if (!message.hasText()) {
            return null;
        }

        String[] tokens = message.getText().trim().split(" ", 2);
        String command = tokens[0];

        return switch (command) {
            case "/start" -> handleStartCommand(tokens.length == 2 ? tokens[1] : null, user);
            case "/create_event" -> handleCreateEventCommand(user);
            case "/my_events" -> handleMyEventsCommand(user);
            case "/feedback" -> handleFeedbackCommand(user);
            default -> handleConversationStatus(message, user);
        };
    }

    private SendMessage handleStartCommand(String params, User user) {
        String chatId = user.getId().toString();

        if (params == null) {
            resetUserConversation(user);
            return new SendMessage(chatId, getHelpMessage());
        }

        String botAnswer;
        String[] tokens = params.split("__", 2);

        if (tokens.length != 2) {
            botAnswer = "Something went wrong!";
        }
        else if (tokens[0].equals("createEvent")) {
            Long eventId = Long.parseLong(tokens[1]);
            user.setConversationStatus(ConversationStatus.CreateFeedback);
            user.setSelectedEventId(eventId);
            userService.insertOrUpdate(user);
            botAnswer = "Please, write feedback for event " + eventService.findById(eventId).getName();
        } else {
            botAnswer = "Something went wrong!";
        }

        return new SendMessage(chatId, botAnswer);
    }

    private SendMessage handleCreateEventCommand(User user) {
        String text = "Please, write in following format:\nevent name - event description";
        user.setConversationStatus(ConversationStatus.CreateEvent);
        userService.insertOrUpdate(user);
        return new SendMessage(user.getId().toString(), text);
    }

    private SendMessage handleMyEventsCommand(User user) {
        String chatId = user.getId().toString();

        resetUserConversation(user);

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

    private SendMessage handleFeedbackCommand(User user) {
        String chatId = user.getId().toString();

        resetUserConversation(user);

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
            for (Feedback feedback : event.getFeedbacks()) {
                builder.append(String.format("-> %s\n", feedback.getMessage()));
            }
        }

        String botAnswer = builder.isEmpty()
                ? "Can't find new feedback for your events."
                : builder.toString();

        return new SendMessage(chatId, botAnswer);
    }

    private SendMessage handleConversationStatus(Message message, User user) {
        return switch (user.getConversationStatus()) {
            case CreateEvent -> handleCreateEventStatus(message, user);
            case CreateFeedback -> handleCreateFeedbackStatus(message, user);
            default -> new SendMessage(user.getId().toString(), getUnknownCommandMessage());
        };
    }

    private SendMessage handleCreateEventStatus(Message message, User user) {
        String chatId = user.getId().toString();

        String[] parts = message.getText().split(" - ", 2);

        resetUserConversation(user);

        if (parts.length != 2) {
            String botAnswer = "Incorrect format!";
            return new SendMessage(chatId, botAnswer);
        }

        String eventName = parts[0];
        String eventDescription = parts[1];
        Event event = new Event(eventName, eventDescription, user);
        event = eventService.createEvent(event);

        String botAnswer = String.format("Event name: %s\nDescription: %s", eventName, eventDescription);

        InlineKeyboardButton button = new InlineKeyboardButton("Send feedback");
        button.setUrl(telegramBotUrl + "createEvent__" + event.getId());

        InlineKeyboardMarkup markup =
                new InlineKeyboardMarkup(Collections.singletonList(Collections.singletonList(button)));

        SendMessage sendMessage = new SendMessage(chatId, botAnswer);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    private SendMessage handleCreateFeedbackStatus(Message message, User user) {
        String chatId = user.getId().toString();

        Long selectedEventId = user.getSelectedEventId();
        if (selectedEventId == null) {
            String botAnswer = "Something went wrong!";
            return new SendMessage(chatId, botAnswer);
        }

        Event selectedEvent = eventService.findById(selectedEventId);
        Feedback feedback = new Feedback(message.getText(), selectedEvent, user);
        feedbackService.createFeedback(feedback);

        resetUserConversation(user);

        String botAnswer = "Successfully send!";
        return new SendMessage(chatId, botAnswer);
    }

    private String getHelpMessage() {
        return "Hi! This bot allows you to register your events and get anonymous feedback from other users. Moreover, you can chat with them!\n"
                + "My commands:\n"
                + "/start\n"
                + "/feedback\n"
                + "/my_events\n"
                + "/create_event";
    }

    private String getUnknownCommandMessage() {
        return "Sorry, I can't understand you.\nPlease, write /start";
    }

    private void resetUserConversation(User user) {
        user.setConversationStatus(ConversationStatus.Default);
        user.setSelectedEventId(null);
        userService.insertOrUpdate(user);
    }
}
