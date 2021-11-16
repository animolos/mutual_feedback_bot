package ru.home.mutual_feedback_bot.api;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

@Slf4j
@Component
public class TelegramFacade {
    private String botNotUnderstandAnswer() { return "Sorry, I can't understand you.\nPlease, write /help"; }

    private final Map<Long, ConversationStatus> userConversationStatus = new HashMap<>();
    private final Map<Long, UUID> userSelectedEvent = new HashMap<>();
    private final Map<Long, HashSet<UUID>> userEvents = new HashMap<>();
    private final Map<UUID, HashSet<String>> eventFeedbacks = new HashMap<>();

    private final Map<UUID, Pair<String, String>> events = new HashMap<>();


    public SendMessage handleUpdate(Update update) {
        String botAnswer = botNotUnderstandAnswer();
        InlineKeyboardMarkup markup = null;

        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (userConversationStatus.containsKey(message.getChatId())) {
                Pair<String, InlineKeyboardMarkup> result = handleConversationStatus(message);
                botAnswer = result.getKey();
                markup = result.getValue();
            }
            else if (message.hasText()) {
                botAnswer = handleInputMessage(message);
            }

            SendMessage reply = new SendMessage(message.getChatId().toString(), botAnswer);

            if (markup != null)
                reply.setReplyMarkup(markup);

            return reply;
        } else if (update.hasCallbackQuery()) {
            botAnswer = handleCallbackQuery(update.getCallbackQuery());

            return new SendMessage(update.getCallbackQuery().getMessage().getChatId().toString(), botAnswer);
        } else {
            return null;
        }
    }

    private Pair<String, InlineKeyboardMarkup> handleConversationStatus(Message message) {
        Long chatId = message.getChatId();
        String botAnswer = botNotUnderstandAnswer();
        String telegramBotUrl = "https://t.me/bro_en_test_bot?start=";
        InlineKeyboardMarkup markup = null;

        ConversationStatus conversationStatus = userConversationStatus.get(chatId);
        userConversationStatus.remove(chatId);

        switch (conversationStatus) {
            case CreateEvent:
                UUID uuid = UUID.randomUUID();

                String[] parts = message.getText().split(" - ", 2);

                if (parts.length != 2) {
                    botAnswer = "Incorrect format!";
                    break;
                }

                String eventName = parts[0];
                String eventDescription = parts[1];

                events.put(uuid, new Pair<>(eventName, eventDescription));

                if (userEvents.containsKey(chatId)) {
                    userEvents.get(chatId).add(uuid);
                } else {
                    userEvents.put(chatId, new HashSet<>(Arrays.asList(uuid)));
                }

                botAnswer = String.format("Event name: %s\nDescription: %s", eventName, eventDescription);

                markup = new InlineKeyboardMarkup();

                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText("Send feedback");
                button.setCallbackData("feedback_" + uuid);
                button.setUrl(telegramBotUrl + uuid);
                rowInline.add(button);
                rowsInline.add(rowInline);
                markup.setKeyboard(rowsInline);
                break;
            case CreateFeedback:
                botAnswer = "Successfully send!";
                if (userSelectedEvent.containsKey(message.getChatId())) {
                    UUID event = userSelectedEvent.get(message.getChatId());

                    if (eventFeedbacks.containsKey(event)) {
                        eventFeedbacks.get(event).add(message.getText());
                    } else {
                        eventFeedbacks.put(event, new HashSet<>(Arrays.asList(message.getText())));
                    }
                } else {
                    botAnswer = "Something went wrong!";
                }
                break;
        }

        return new Pair<>(botAnswer, markup);
    }

    private String handleCallbackQuery(CallbackQuery query) {
        String queryData = query.getData();
        long chatId = query.getMessage().getChatId();

        if (queryData.startsWith("feedback_")) {
            UUID event = UUID.fromString(queryData.replaceFirst("feedback_", ""));

            userSelectedEvent.put(chatId, event);
            userConversationStatus.put(chatId, ConversationStatus.CreateFeedback);
        }

        return "Please, write your feedback for event";
    }

    private String handleInputMessage(Message message) {
        String botAnswer = "Sorry, I can't understand you.\nPlease, write /help";

        String helpMessage = "Hi! This bot allows you to register your events and get anonymous feedback from other users. Moreover, you can chat with them!\n"
                + "My commands:\n"
                + "/start\n"
                + "/help\n"
                + "/feedback\n"
                + "/my_events\n"
                + "/create_event";

        Long chatId = message.getChatId();

        String text = message.getText();

        switch (text) {
            case "/start":
                botAnswer = helpMessage;
                break;
            case "/help":
                botAnswer = helpMessage;
                break;
            case "/create_event":
                botAnswer = "Please, write in following format:\nevent name - event description";
                userConversationStatus.put(chatId, ConversationStatus.CreateEvent);
                break;
            case "/my_events":
                if (userEvents.containsKey(chatId) && userEvents.get(chatId).size() > 0) {
                    ArrayList<UUID> evts = new ArrayList<>(userEvents.get(chatId));
                    Collections.sort(evts);
                    StringBuffer strBuffer = new StringBuffer();
                    for (UUID event: evts) {
                        strBuffer.append(String.format("- %s\n", events.get(event).getKey()));
                    }
                    botAnswer = "Your events:\n" + strBuffer;
                } else {
                    botAnswer = "You didn't create any event yet!\nPlease, write /create_event";
                }
                break;
            case "/feedback":
                if (!userEvents.containsKey(chatId)) {
                    botAnswer = "You didn't create any event yet!\nPlease, write /create_event";
                    break;
                }

                HashSet<UUID> evts = userEvents.get(chatId);

                StringBuffer strBuffer = new StringBuffer();

                for (UUID event: evts) {
                    if (eventFeedbacks.containsKey(event)) {
                        HashSet<String> feedbacks = eventFeedbacks.get(event);
                        if (feedbacks.size() > 0) {
                            strBuffer.append(String.format("Event: %s\n", events.get(event).getKey()));
                            for (String feedback : feedbacks) {
                                strBuffer.append(String.format("-> %s\n", feedback));
                            }
                            eventFeedbacks.put(event, new HashSet<>());
                        }
                    }
                }

                if (strBuffer.capacity() == 16) {
                    botAnswer = "Can't find new feedback for your events.";
                } else {
                    botAnswer = strBuffer.toString();
                }
                break;
        }

        if (text.startsWith("/start") && text.length() > 7) {
            String[] parts = text.split(" ", 2);
            userConversationStatus.put(message.getChatId(), ConversationStatus.CreateFeedback);
            userSelectedEvent.put(message.getChatId(), UUID.fromString(parts[1]));
            botAnswer = "Please, write feedback for event " + events.get(UUID.fromString(parts[1])).getKey();
        }

        return botAnswer;
    }

    private enum ConversationStatus {
        CreateEvent,
        CreateFeedback,
    }
}
