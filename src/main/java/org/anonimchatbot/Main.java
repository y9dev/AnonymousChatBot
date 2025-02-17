package org.anonimchatbot;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;



public class Main {
    public static void main(String[] args) {
        Database.initDatabase();
        TelegramBotsLongPollingApplication application = new TelegramBotsLongPollingApplication();
        try {
                application.registerBot(Constants.BOT_TOKEN, new Bot());
            System.out.println("Bot has initialized");
        } catch (TelegramApiException e) {
            e.printStackTrace(System.err);
        }
    }
}