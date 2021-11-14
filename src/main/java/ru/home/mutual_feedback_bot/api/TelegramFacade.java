package ru.home.mutual_feedback_bot.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Slf4j
@Component
public class TelegramFacade {

    public SendMessage handleUpdate(Update update) {
        SendMessage replyMessage = null;

        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            log.info("New message from User:{}, chatId: {},  with text: {}",
                    message.getFrom().getUserName(), message.getChatId(), message.getText());
            replyMessage = handleInputMessage(message);
        }

        return replyMessage;
    }

    private SendMessage handleInputMessage(Message message) {
        String botAnswer = "Sorry, I can't understand you.\nPlease, write /help";

        String helpMessage = "Hi! This bot allows you to register your events and get anonymous feedback from other users. Moreover, you can chat with them!\n"
                + "My commands:\n"
                + "/start\n"
                + "/help\n"
                + "/feedback\n"
                + "/my_events\n"
                + "/create_event\n"
                + "/create_feedback";

        Long chatId = message.getChatId();

        if (chatConversationStatus.containsKey(chatId)) {
            ConversationStatus conversationStatus = chatConversationStatus.get(chatId);
            chatConversationStatus.remove(chatId);

            switch (conversationStatus) {
                case CreateEvent:
                    botAnswer = "Successfully created!";
                    if (chatEvents.containsKey(chatId)) {
                        if (chatEvents.get(chatId).contains(message.getText())) {
                            botAnswer = "Sorry, you already have event with this name!";
                        } else {
                            chatEvents.get(chatId).add(message.getText());
                        }
                    } else {
                        chatEvents.put(chatId, new HashSet<>(Arrays.asList(message.getText())));
                    }
                    break;
                case CreateFeedback:
                    botAnswer = "Successfully send!";
                    String[] splitValues = message.getText().split(" - ", 2);

                    if (splitValues.length != 2) {
                        botAnswer = "Incorrect format! This should be XXX - YYY";
                    } else {
                        if (chatEvents.containsKey(chatId) && chatEvents.get(chatId).contains(splitValues[0])) {
                            if (eventFeedbacks.containsKey(splitValues[0])) {
                                eventFeedbacks.get(splitValues[0]).add(splitValues[1]);
                            } else {
                                eventFeedbacks.put(splitValues[0], Arrays.asList(splitValues[1]));
                            }
                        } else {
                            botAnswer = "Can't find event with that name!";
                        }
                    }
                    break;
                case GetFeedbacks:
                    if (eventFeedbacks.containsKey(message.getText())) {
                        botAnswer = "Your feedbacks:\n" + String.join("\n", eventFeedbacks.get(message.getText()));
                    } else {
                        if (chatEvents.containsKey(chatId) && chatEvents.get(chatId).contains(message.getText())) {
                            botAnswer = "There are no feedback for this event yet";
                        } else {
                            botAnswer = "Event with that name doesn't exist";
                        }
                    }
                    break;
            }
        } else {
            switch (message.getText()) {
                case "/start":
                    botAnswer = helpMessage;
                    break;
                case "/help":
                    botAnswer = helpMessage;
                    break;
                case "/create_event":
                    botAnswer = "Please, enter event name";
                    chatConversationStatus.put(chatId, ConversationStatus.CreateEvent);
                    break;
                case "/my_events":
                    if (chatEvents.containsKey(chatId) && chatEvents.get(chatId).size() > 0) {
                        ArrayList<String> subjects = new ArrayList<>(chatEvents.get(chatId));
                        Collections.sort(subjects);
                        botAnswer = "Your events:\n" + String.join("\n", subjects);
                    } else {
                        botAnswer = "You didn't create any event yet!\nPlease, write /create_event";
                    }
                    break;
                case "/feedback":
                    botAnswer = "Please, enter event name";
                    chatConversationStatus.put(chatId, ConversationStatus.GetFeedbacks);
                    break;
                case "/create_feedback":
                    botAnswer = "Please, enter event name and feedback in following order:\nXXX - YYY";
                    chatConversationStatus.put(chatId, ConversationStatus.CreateFeedback);
                    break;
        }}

        return new SendMessage(message.getChatId().toString(), botAnswer);
    }

    private final Map<Long, ConversationStatus> chatConversationStatus = new HashMap<>();
    private final Map<Long, HashSet<String>> chatEvents = new HashMap<>();
    private final Map<String, List<String>> eventFeedbacks = new HashMap<>();

    private enum ConversationStatus {
        CreateEvent,
        CreateFeedback,
        GetFeedbacks,
    }
}
