package org.anonimchatbot;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient = new OkHttpTelegramClient(Constants.BOT_TOKEN);

    @Override
    public void consume(Update update) {
        // Стартовое сообщение при команде /start
        if (update.hasMessage() && update.getMessage().getChat().isUserChat()) {
            Message umessage = update.getMessage();
            // Отправляет стартовое сообщение + кнопку "Отправить сообщение"
            if (umessage.hasText() && umessage.getText().equals("/start")) {
                Database.addUser(umessage.getChatId().toString());
                String startMessage = """
                        \uD83D\uDD35 Добро пожаловать в анонимный чат-бот!\s
                        
                        \uD83D\uDCAC Этот бот позволяет вам общаться с администрацей, оставаясь полностью анонимным.
                        
                        \uD83D\uDCE9 Как это работает?
                        
                        1️⃣ Отправьте сообщение — бот передаст его администрации.
                        
                        2️⃣ Получите ответ — переписка остаётся анонимной.
                        
                        \uD83D\uDE80 Начните анонимный чат с администрацией прямо сейчас!""";
                sendMessage(umessage.getChatId(), startMessage, true);
            }
            // Реакция на нажатие кнопки "Отправить сообщение"
            else if (umessage.hasText() && umessage.getText().equals("\uD83D\uDCE9 Отправить сообщение")) {
                if (Database.getBanned(umessage.getChatId().toString()).equals(Constants.getBanned_BANNED())) {
                    removeMessage(umessage.getChatId(), umessage.getMessageId().toString());
                    sendMessage(umessage.getChatId(), """
                            ❌ Ошибка:
                            
                            🔴 Вы заблокированы\\!""");
                } else {
                    Database.updWaitStatus(Constants.getStatus_WAIT(), umessage.getChatId().toString());
                    removeMessage(umessage.getChatId(), umessage.getMessageId().toString());
                    sendMessage(umessage.getChatId(), """
                            📝 Введите текст вашего анонимного сообщения...
                            
                            🔵 Оно будет отправлено администрации без указания вашего имени.""", false);
                }
            }
            // Реакция на обычные сообщения
            else {
                if (umessage.hasText() && Database.getWaitStatus(umessage.getChatId().toString()).equals(Constants.getStatus_WAIT())) {
                    removeMessage(umessage.getChatId(), Database.getMessageId(umessage.getChatId().toString()));
                    sendMessage(umessage.getChatId(), """
                            📤 Ваше сообщение отправлено анонимно!
                            
                            💙 Ожидайте ответа от администрации!""", true);
                    Database.updWaitStatus(Constants.getStatus_STAY(), umessage.getChatId().toString());
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), "\uD83D\uDCE4 Анонимное сообщение отправлено\\!\n" +
                            "\n" +
                            "\uD83D\uDCAC Текст:\n>" + escapeMarkdownV2(umessage.getText()), umessage.getChatId());
                } else {
                    removeMessage(umessage.getChatId(), umessage.getMessageId().toString());
                }
            }
        }

        // Реагирует на сообщения из чата администрации
        else if (update.hasMessage() && update.getMessage().getChatId().toString().equals(Constants.ADMIN_CHAT_ID)) {
            Database.addPermUser(update.getMessage().getFrom().getUserName());
            Message umessage = update.getMessage();
            if (umessage.getNewChatMembers() != null) {
                for (User newUser : umessage.getNewChatMembers()) {
                    Database.addPermUser(newUser.getUserName());
                }
            }
            if (umessage.isReply() && umessage.getReplyToMessage().getFrom().getIsBot() && umessage.hasText()) {
                String chatId = Database.getChatIdFromGroupMessages(umessage.getReplyToMessage().getMessageId().toString());
                long chatIdAdm = Long.parseLong(Constants.ADMIN_CHAT_ID);
                if (chatId != null) {
                    if (umessage.getText().equalsIgnoreCase("бан")) {
                        if (Database.getBanned(chatId).equals(Constants.getBanned_BANNED())) {
                            sendMessage(chatIdAdm, """
                                    ❌ Ошибка:
                                    
                                    🔴 Пользователь уже заблокирован\\!""");
                        }
                        else {
                            Database.banUser(chatId);
                            sendMessage(chatIdAdm, """
                                    🔴 Блокировка:
                                    
                                    🚨 Пользователь заблокирован\\!""");
                            sendMessage(Long.parseLong(chatId), """
                                🔴 Блокировка:
                                
                                🚨 Вы были заблокированы!""", false);
                        }
                    }
                    else if (umessage.getText().equalsIgnoreCase("разбан")) {
                        if (Database.getBanned(chatId).equals(Constants.getBanned_BANNED())) {
                            Database.pardonUser(chatId);
                            sendMessage(chatIdAdm, """
                                    🟢 Разблокировка:
                                    
                                    ✅ Пользователь разблокирован\\!""");
                            sendMessage(Long.parseLong(chatId), """
                                    🟢 Разблокировка:
                                    
                                    ✅ Вы были разблокированы!""", true);
                        }

                        else {
                            Database.banUser(chatId);
                            sendMessage(chatIdAdm, """
                                    ❌ Ошибка:
                                    
                                    🔴 Пользователь не заблокирован\\!""");
                        }
                    }
                    else {
                        if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN())
                                || Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                            sendMessage(Long.parseLong(chatId), """
                                 📬 Вам пришел ответ\\!
                                \s
                                 💬 Текст:\s
                                >""" + escapeMarkdownV2( umessage.getText()));
                            sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                    📤 Ответ отправлен\\!""");
                        } else {
                            sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                            ❌ Ошибка:
                            
                            🔴 Отвечать могут только администраторы\\!""");
                        }

                    }
                }
                else if (umessage.getText().equalsIgnoreCase("разбан")
                        || umessage.getText().equalsIgnoreCase("бан")) {
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                            ❌ Ошибка:
                            
                            🔴 Отвечайте на анонимное сообщение от бота\\!""");
                }
                else if (umessage.isReply()
                        || umessage.getText().equalsIgnoreCase("повысить")
                        && umessage.getText().equalsIgnoreCase("понизить")) {
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                ❌ Ошибка:
                                
                                🔴 Команду нельзя использовать на боте\\!""");
                }
            }
            else if (umessage.isReply() && !umessage.getReplyToMessage().getFrom().getIsBot() && umessage.hasText()) {
                long chatIdAdm = Long.parseLong(Constants.ADMIN_CHAT_ID);
                if (umessage.getText().equalsIgnoreCase("повысить")) {
                    if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                            Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                    umessage.getReplyToMessage().getFrom().getUserName().equals(umessage.getFrom().getUserName())) {
                        sendMessage(chatIdAdm, """
                                ❌ Ошибка:
                                
                                🔴 Вы не можете повысить сами себя\\!""");
                    }
                    else if(!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                        Database.addPermUser(umessage.getFrom().getUserName());
                        sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                ❌ Ошибка:
                                
                                🔴 Вы не являетесь супер админом\\!""");
                    }

                    else if (Database.getPerm(umessage.getReplyToMessage().getFrom().getUserName()).equals(Constants.getPerm_ADMIN())) {
                        sendMessage(chatIdAdm, """
                                ❌ Ошибка:
                                
                                🔴 Пользователь уже является администратором\\!""");
                    }
                    else {
                        Database.upgradePermUser(umessage.getReplyToMessage().getFrom().getUserName());
                        sendMessage(chatIdAdm, """
                                🟢 Изменение прав:
                                
                                ✅ Пользователь успешно повышен\\!""");
                    }
                }
                else if(umessage.getText().equalsIgnoreCase("понизить")) {
                    if(Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                            Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                    umessage.getReplyToMessage().getFrom().getUserName().equals(umessage.getFrom().getUserName())) {
                        sendMessage(chatIdAdm, """
                                ❌ Ошибка:
                                
                                🔴 Вы не можете понизить сами себя\\!""");
                    }
                    else if(!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                        Database.addPermUser(umessage.getFrom().getUserName());
                        sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                ❌ Ошибка:
                                
                                🔴 Вы не являетесь супер админом\\!""");
                    }

                    else if (!Database.getPerm(umessage.getReplyToMessage().getFrom().getUserName()).equals(Constants.getPerm_ADMIN())) {
                        sendMessage(chatIdAdm, """
                                ❌ Ошибка:
                                
                                🔴 Пользователь не является администратором\\!""");
                    }
                    else {
                        Database.demotePermUser(umessage.getReplyToMessage().getFrom().getUserName());
                        sendMessage(chatIdAdm, """
                                🟢 Изменение прав:
                                
                                ✅ Пользователь успешно понижен\\!""");
                    }
                }
                else if (umessage.getText().equalsIgnoreCase("разбан")
                        || umessage.getText().equalsIgnoreCase("бан")) {
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                ❌ Ошибка:
                                
                                🔴 Отвечайте на анонимное сообщение от бота\\!""");
                }
                else if (umessage.isReply() && umessage.getText().equalsIgnoreCase("Кто ты")) {
                    sendMessage(chatIdAdm, "\uD83E\uDD16 Информация о пользователе: \n\n"
                            + "\uD83D\uDC64 Имя: @" + umessage.getReplyToMessage().getFrom().getUserName()
                            +"\n\uD83D\uDD39 Уровень прав: " + parsePerm(Database.getPerm(umessage.getReplyToMessage().getFrom().getUserName())));
                }
            }
            else if (umessage.hasText() && !umessage.isReply() && umessage.getText().equalsIgnoreCase("Кто я")) {
                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), "\uD83E\uDD16 Информация о пользователе: \n\n"
                        + "\uD83D\uDC64 Имя: @" + umessage.getFrom().getUserName()
                        +"\n\uD83D\uDD39 Уровень прав: " + parsePerm(Database.getPerm(umessage.getFrom().getUserName())));
            }
            else if (umessage.hasText() && !umessage.isReply() && umessage.getText().equalsIgnoreCase("команды")) {
                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                        📜 Доступные команды:
                        
                        🔹 бан \\<с ответом на анонимное сообщение\\> – заблокировать пользователя, запретив ему отправлять сообщения\\.
                        
                        🟢 разбан \\<с ответом на анонимное сообщение\\> – разблокировать пользователя, вернув доступ к сообщениям\\.
                        
                        📈 повысить \\<user\\_id\\> /  \\<с ответом сообщение\\> – повысить уровень доступа пользователя\\.
                       
                        📉 понизить \\<user\\_id\\> / \\<с ответом сообщение\\> – понизить уровень доступа пользователя\\.
                        
                        👤 кто я – узнать свой уровень доступа\\.
                        
                        🤖 кто ты – информация о пользователе, чей ID указан или на чье сообщение был ответ\\.
                        
                        🚀 Используйте команды только по назначению\\!""");
            }
            else if (umessage.hasText() && !umessage.isReply()) {
                long chatIdAdm = Long.parseLong(Constants.ADMIN_CHAT_ID);
                if (getUserId(umessage.getText()) != null) {
                    String userId = getUserId(umessage.getText());
                    if (Database.getPerm(getUserId(umessage.getText())) != null) {
                        if (umessage.getText().toLowerCase().startsWith("повысить")) {
                            if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                                    Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                            userId.equals(umessage.getFrom().getUserName())) {
                                sendMessage(chatIdAdm, """
                                        ❌ Ошибка:
                                        
                                        🔴 Вы не можете повысить сами себя\\!""");
                            } else if (!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                        ❌ Ошибка:
                                        
                                        🔴 Вы не являетесь супер админом\\!""");
                            } else if (Database.getPerm(userId).equals(Constants.getPerm_ADMIN())) {
                                sendMessage(chatIdAdm, """
                                        ❌ Ошибка:
                                        
                                        🔴 Пользователь уже является администратором\\!""");
                            } else {
                                Database.upgradePermUser(userId);
                                sendMessage(chatIdAdm, """
                                        🟢 Изменение прав:
                                        
                                        ✅ Пользователь успешно повышен\\!""");
                            }
                        }
                        else if (umessage.getText().toLowerCase().startsWith("понизить")) {
                            if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                                    Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                            userId.equals(umessage.getFrom().getUserName())) {
                                sendMessage(chatIdAdm, """
                                        ❌ Ошибка:
                                        
                                        🔴 Вы не можете понизить сами себя\\!""");
                            } else if (!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                        ❌ Ошибка:
                                        
                                        🔴 Вы не являетесь супер админом\\!""");
                            } else if (!Database.getPerm(userId).equals(Constants.getPerm_ADMIN())) {
                                sendMessage(chatIdAdm, """
                                        ❌ Ошибка:
                                        
                                        🔴 Пользователь не является администратором\\!""");
                            } else {
                                Database.demotePermUser(userId);
                                sendMessage(chatIdAdm, """
                                        🟢 Изменение прав:
                                        
                                        ✅ Пользователь успешно понижен\\!""");
                            }
                        }
                        else if (umessage.getText().toLowerCase().startsWith("кто ты")) {
                            sendMessage(chatIdAdm, "\uD83E\uDD16 Информация о пользователе: \n\n"
                                    + "\uD83D\uDC64 Имя: @" + userId
                                    +"\n\uD83D\uDD39 Уровень прав: " + parsePerm(Database.getPerm(userId)));
                        }
                    }
                    else {
                        sendMessage(chatIdAdm, """
                                ❌ Ошибка:
                                
                                🔴 Пользователя нет в базе данных\\!""");
                    }
                }
                else if (umessage.hasText() && umessage.getText().contains("@") && umessage.getText().toLowerCase().startsWith("повысить") || umessage.getText().toLowerCase().startsWith("понизить")){
                    sendMessage(chatIdAdm, """
                                ❌ Ошибка:
                                
                                🔴 Напишите имя пользователя\\!""");
                }
            }
        }
    }

    public void sendMessage(long chat_id, String text, boolean keyboard) {
        if (keyboard) {
            SendMessage message = SendMessage.builder()
                    .chatId(chat_id)
                    .text(text)
                    .replyMarkup(getKeyboard())
                    .build();

            try {
                Message sentMessage = telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace(System.err);
            }
        } else {
            SendMessage message = SendMessage.builder()
                    .chatId(chat_id)
                    .text(text)
                    .replyMarkup(
                            ReplyKeyboardRemove.builder()
                                    .selective(true)
                                    .removeKeyboard(true)
                                    .build()
                    )
                    .build();
            try {
                Message sentMessage = telegramClient.execute(message);
                    Database.updMessageId(sentMessage.getMessageId().toString(), String.valueOf(chat_id));
            } catch (TelegramApiException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public void sendMessage(long chat_id, String text, long chat_id_user) {
        SendMessage message = SendMessage.builder()
                .chatId(chat_id)
                .parseMode("MarkdownV2")
                .text(text)
                .build();
        try {
            Message sentMessage = telegramClient.execute(message);
            Database.updGroupMessage(String.valueOf(chat_id_user), sentMessage.getMessageId().toString());
        } catch (TelegramApiException e) {
                e.printStackTrace(System.err);
            }
        }
    public void sendMessage(long chat_id, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chat_id)
                .text(text)
                .parseMode("MarkdownV2")
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(System.err);
        }
    }

    public void removeMessage(long chat_id, String message_id) {
        DeleteMessage message = DeleteMessage.builder()
                .messageId(Integer.valueOf(message_id))
                .chatId(chat_id)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(System.err);
        }
    }
    public ReplyKeyboardMarkup getKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("\uD83D\uDCE9 Отправить сообщение"));
        ReplyKeyboardMarkup markup = ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .oneTimeKeyboard(true)
                .isPersistent(false)
                .resizeKeyboard(true)
                .selective(true)
                .build();
        return markup;
    }
    public String getUserId(String message) {
        String[] lines = message.split("\n");
        Pattern pt = Pattern.compile("@(\\w+)");
        for (String line : lines) {
            Matcher mt = pt.matcher(line);
            if (mt.find()) {
                return mt.group(1);
            }
        }
        return null;
    }
    public String parsePerm(String perm) {
        return switch (perm) {
            case "USER" -> "Пользователь";
            case "ADMIN" -> "Админ";
            case "SUPER_ADMIN" -> "Супер\\-админ";
            default -> null;
        };
    }
    public static String escapeMarkdownV2(String text) {
        return text.replaceAll("([\\\\_*()~`>#+\\-=|{}.!])", "\\\\$1");
    }
}
