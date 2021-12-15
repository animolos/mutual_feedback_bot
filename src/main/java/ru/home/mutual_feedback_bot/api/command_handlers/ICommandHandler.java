package ru.home.mutual_feedback_bot.api.command_handlers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.home.mutual_feedback_bot.entities.User;

public interface ICommandHandler {

    public boolean accept(String command);

    public SendMessage handle(User user, String[] args);
}
