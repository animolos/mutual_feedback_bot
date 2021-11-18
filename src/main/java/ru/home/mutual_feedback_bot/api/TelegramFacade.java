package ru.home.mutual_feedback_bot.api;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
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

    private final Map<UUID, ConversationStatus> userConversationStatus = new HashMap<>();
    private final Map<UUID, UUID> userSelectedEvent = new HashMap<>();
    private final Map<UUID, UUID> userSelectedMessageToReply = new HashMap<>();
    private final Map<UUID, HashSet<UUID>> userEvents = new HashMap<>();
    private final Map<UUID, HashSet<UUID>> eventFeedbacks = new HashMap<>();

    private final Map<UUID, Pair<String, String>> eventsInfo = new HashMap<>();
//    private final Map<UUID, Long> eventCreatorId = new HashMap<>();
    private final Map<UUID, String> messageIdToMessage = new HashMap<>();
    private final Map<UUID, UUID> messageIdToUserId = new HashMap<>();

    private final Map<UUID, Long> idToUserId = new HashMap<>();
    private final Map<Long, UUID> userIdToId = new HashMap<>();


    public Pair<SendMessage, SendMessage> handleUpdate(Update update) {
        String botAnswer = botNotUnderstandAnswer();
        InlineKeyboardMarkup markup = null;
        SendMessage otherChatMessage = null;

        if (update.hasMessage()) {
            Message message = update.getMessage();

            Long chatId = message.getChatId();

            if (!userIdToId.containsKey(chatId)) {
                userIdToId.put(chatId, UUID.randomUUID());
            }

            UUID userId = userIdToId.get(chatId);

            if (!idToUserId.containsKey(userId)) {
                idToUserId.put(userId, chatId);
            }

            if (userConversationStatus.containsKey(userIdToId.get(chatId))) {
                Pair<Pair<String, InlineKeyboardMarkup>, SendMessage> result = handleConversationStatus(message);
                Pair<String, InlineKeyboardMarkup> bot = result.getKey();
                botAnswer = bot.getKey();
                markup = bot.getValue();
                otherChatMessage = result.getValue();
            }
            else if (message.hasText()) {
                Pair<String, InlineKeyboardMarkup> result = handleInputMessage(message);
                botAnswer = result.getKey();
                markup = result.getValue();
            }

            SendMessage reply = new SendMessage();//message.getChatId().toString(), botAnswer);
            reply.setChatId(message.getChatId().toString());
            reply.setText(botAnswer);
            reply.setParseMode(ParseMode.HTML);

            if (markup != null)
                reply.setReplyMarkup(markup);

            return new Pair<>(reply, otherChatMessage);
        } else if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (!userIdToId.containsKey(chatId)) {
                userIdToId.put(chatId, UUID.randomUUID());
            }

            UUID userId = userIdToId.get(chatId);

            if (!idToUserId.containsKey(userId)) {
                idToUserId.put(userId, chatId);
            }

            botAnswer = handleCallbackQuery(update.getCallbackQuery());

            return new Pair<>(new SendMessage(update.getCallbackQuery().getMessage().getChatId().toString(), botAnswer), null);
        } else {
            return null;
        }
    }

    private Pair<Pair<String, InlineKeyboardMarkup>, SendMessage> handleConversationStatus(Message message) {
        Long chatId = message.getChatId();
        String botAnswer = botNotUnderstandAnswer();
        String telegramBotUrl = "https://t.me/bro_en_test_bot?start=";
        InlineKeyboardMarkup markup = null;

        SendMessage otherUserSendMessage = null;

        UUID userId = userIdToId.get(chatId);

        ConversationStatus conversationStatus = userConversationStatus.get(userId);
        userConversationStatus.remove(userId);

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

                eventsInfo.put(uuid, new Pair<>(eventName, eventDescription));
//                eventCreatorId.put(uuid, chatId);

                if (userEvents.containsKey(userId)) {
                    userEvents.get(userId).add(uuid);
                } else {
                    userEvents.put(userId, new HashSet<>(Arrays.asList(uuid)));
                }

                botAnswer = String.format("Event name: %s\nDescription: %s", eventName, eventDescription);

                markup = new InlineKeyboardMarkup();

                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText("Send feedback");
                button.setCallbackData("feedback_" + uuid);
                button.setUrl(telegramBotUrl + "createEvent__" + uuid);
                rowInline.add(button);
                rowsInline.add(rowInline);
                markup.setKeyboard(rowsInline);
                break;
            case CreateFeedback:
                botAnswer = "Successfully send!";
                if (userSelectedEvent.containsKey(userId)) {
                    UUID event = userSelectedEvent.get(userId);
                    UUID messageId = UUID.randomUUID();
                    messageIdToMessage.put(messageId, message.getText());
                    messageIdToUserId.put(messageId, userId);
                    if (eventFeedbacks.containsKey(event)) {
                        eventFeedbacks.get(event).add(messageId);
                    } else {
                        eventFeedbacks.put(event, new HashSet<>(Arrays.asList(messageId)));
                    }
                } else {
                    botAnswer = "Something went wrong!";
                }
                break;
            case Reply:
                botAnswer = "Successfully send!";
                UUID messageToReply = userSelectedMessageToReply.get(userId);
                UUID userToReply = messageIdToUserId.get(messageToReply);
                Long user = idToUserId.get(userToReply);
                String msg = messageIdToMessage.get(messageToReply);

                String otherUserBotAnswer = "Error while replying to your message";
                InlineKeyboardMarkup otherUserBotMarkup = new InlineKeyboardMarkup();

                String jj = "There are reply to your feedback!\nMessage: " + msg + "\nReply: " + message.getText();

                UUID msgId = UUID.randomUUID();
                messageIdToMessage.put(msgId, message.getText());
                messageIdToUserId.put(msgId, userId);

                List<List<InlineKeyboardButton>> o_rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> o_rowInline = new ArrayList<>();
                InlineKeyboardButton o_button = new InlineKeyboardButton();
                o_button.setText("Reply");
                o_button.setCallbackData("reply_" + message);
                o_button.setUrl(telegramBotUrl + "reply__" + msg);
                o_rowInline.add(o_button);
                o_rowsInline.add(o_rowInline);
                otherUserBotMarkup.setKeyboard(o_rowsInline);

                otherUserSendMessage = new SendMessage(user.toString(), jj);
                otherUserSendMessage.setReplyMarkup(otherUserBotMarkup);
        }

        return new Pair<>(new Pair<>(botAnswer, markup), otherUserSendMessage);
    }

    private String handleCallbackQuery(CallbackQuery query) {
        String queryData = query.getData();
        long chatId = query.getMessage().getChatId();

        UUID userId = userIdToId.get(chatId);

        if (queryData.startsWith("feedback_")) {
            UUID event = UUID.fromString(queryData.replaceFirst("feedback_", ""));

            userSelectedEvent.put(userId, event);
            userConversationStatus.put(userId, ConversationStatus.CreateFeedback);

            return "Please, write your feedback for event";
        } else if (queryData.startsWith("reply_")) {
            UUID messageId = UUID.fromString(queryData.replaceFirst("reply_", ""));

            userSelectedMessageToReply.put(userId, messageId);
            userConversationStatus.put(userId, ConversationStatus.Reply);

            return "Please, write reply to message";
        }

        return botNotUnderstandAnswer();
    }

    private Pair<String, InlineKeyboardMarkup> handleInputMessage(Message message) {
        String botAnswer = "Sorry, I can't understand you.\nPlease, write /help";
        String telegramBotUrl = "https://t.me/bro_en_test_bot?start=";
        InlineKeyboardMarkup markup = null;

        String helpMessage = "Hi! This bot allows you to register your events and get anonymous feedback from other users. Moreover, you can chat with them!\n"
                + "My commands:\n"
                + "/start\n"
                + "/help\n"
                + "/feedback\n"
                + "/my_events\n"
                + "/create_event";

        Long chatId = message.getChatId();

        UUID userId = userIdToId.get(chatId);

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
                userConversationStatus.put(userId, ConversationStatus.CreateEvent);
                break;
            case "/my_events":
                if (userEvents.containsKey(userId) && userEvents.get(userId).size() > 0) {
                    ArrayList<UUID> evts = new ArrayList<>(userEvents.get(userId));
                    Collections.sort(evts);
                    StringBuffer strBuffer = new StringBuffer();
                    for (UUID event: evts) {
                        strBuffer.append(String.format("- %s\n", String.format("<a href=\"%s\">%s</a>", telegramBotUrl + "event__" + event, eventsInfo.get(event).getKey())));
                    }
                    botAnswer = "Your events:\n" + strBuffer;
                } else {
                    botAnswer = "You didn't create any event yet!\nPlease, write /create_event";
                }
                break;
            case "/feedback":
                if (!userEvents.containsKey(userId)) {
                    botAnswer = "You didn't create any event yet!\nPlease, write /create_event";
                    break;
                }

                HashSet<UUID> evts = userEvents.get(userId);

                StringBuffer strBuffer = new StringBuffer();

                for (UUID event: evts) {
                    if (eventFeedbacks.containsKey(event)) {
                        HashSet<UUID> feedbacks = eventFeedbacks.get(event);
                        if (feedbacks.size() > 0) {
                            strBuffer.append(String.format("Event: %s\n", eventsInfo.get(event).getKey()));
                            for (UUID feedbackId : feedbacks) {
                                strBuffer.append(String.format("(%s) -> %s\n", String.format("<a href=\"%s\">%s</a>", telegramBotUrl + "reply__" + feedbackId, "reply"), messageIdToMessage.get(feedbackId)));
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
            String[] parts1 = parts[1].split("__", 2);
            if (parts1.length != 2) {
                return new Pair<>("Something went wrong!", null);
            }
            if (Objects.equals(parts1[0], "createEvent")) {
                userConversationStatus.put(userId, ConversationStatus.CreateFeedback);
                userSelectedEvent.put(userId, UUID.fromString(parts1[1]));
                botAnswer = "Please, write feedback for event " + eventsInfo.get(UUID.fromString(parts1[1])).getKey();
            } else if (Objects.equals(parts1[0], "event")) {
                log.info("HEHEHE");
            } else if (Objects.equals(parts1[0], "reply")) {
                // messageId in reply

                UUID messageId = UUID.fromString(parts1[1]);
                userConversationStatus.put(userId, ConversationStatus.Reply);
                userSelectedMessageToReply.put(userId, messageId);
                botAnswer = "Write reply to message:\n" + messageIdToMessage.get(messageId);
//                botAnswer = "Message:\n" + messageIdToMessage.get(messageId);

//                markup = new InlineKeyboardMarkup();
//
//                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
//                List<InlineKeyboardButton> rowInline = new ArrayList<>();
//                InlineKeyboardButton button = new InlineKeyboardButton();
//                button.setText("Reply");
//                button.setCallbackData("reply_" + messageId);
//                button.setUrl(telegramBotUrl + "reply__" + messageId);
//                rowInline.add(button);
//                rowsInline.add(rowInline);
//                markup.setKeyboard(rowsInline);
            } else {
                return new Pair<>("Smth went wrong!", null);
            }
        }

        return new Pair<>(botAnswer, markup);
    }

    private enum ConversationStatus {
        CreateEvent,
        CreateFeedback,
        Reply,
    }
}
