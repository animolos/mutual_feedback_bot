package ru.home.mutual_feedback_bot.extensions;

public class Strings {
    public static String getHelpMessage() {
        return "Hi! This bot allows you to register your events and get anonymous feedback from other users. Moreover, you can chat with them!\n"
                + "My commands:\n"
                + "/start\n"
                + "/feedback\n"
                + "/my_events\n"
                + "/create_event\n"
                + "/replies";
    }

    public static String getUnknownCommandMessage() {
        return "Sorry, I can't understand you.\nPlease, write /start";
    }
}
