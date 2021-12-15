package ru.home.mutual_feedback_bot.api.status_handlers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.home.mutual_feedback_bot.entities.User;
import ru.home.mutual_feedback_bot.models.ConversationStatus;

public interface IStatusHandler {
    public boolean accept(ConversationStatus status);

    public SendMessage handle(Message message, User user);
}


