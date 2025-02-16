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
        // decoment sout and start bot, send msg to adm chat and paste to constants
//        System.out.println(update.getMessage().getChatId());
        // Start message for the /start command
        if (update.hasMessage() && update.getMessage().getChat().isUserChat()) {
            Message umessage = update.getMessage();
            // Sends a start message + "Send Message" button

            if (umessage.hasText() && umessage.getText().equals("/start")) {
                Database.addUser(umessage.getChatId().toString());
                String startMessage = """
                        \uD83D\uDD35 Welcome to the anonymous chat bot!\s
                        
                        \uD83D\uDCAC This bot allows you to communicate with the administration while remaining completely anonymous.
                        
                        \uD83D\uDCE9 How does it work?
                        
                        1️⃣ Send a message — the bot will forward it to the administration.
                        
                        2️⃣ Receive a response — the conversation remains anonymous.
                        
                        \uD83D\uDE80 Start an anonymous chat with the administration right now!""";
                sendMessage(umessage.getChatId(), startMessage, true);
            }
            // Reaction to pressing the "Send Message" button
            else if (umessage.hasText() && umessage.getText().equals("\uD83D\uDCE9 Send Message")) {
                if (Database.getBanned(umessage.getChatId().toString()).equals(Constants.getBanned_BANNED())) {
                    removeMessage(umessage.getChatId(), umessage.getMessageId().toString());
                    sendMessage(umessage.getChatId(), """
                            ❌ Error:
                            
                            🔴 You are banned\\!""");
                } else {
                    Database.updWaitStatus(Constants.getStatus_WAIT(), umessage.getChatId().toString());
                    removeMessage(umessage.getChatId(), umessage.getMessageId().toString());
                    sendMessage(umessage.getChatId(), """
                            📝 Enter the text of your anonymous message...
                            
                            🔵 It will be sent to the administration without revealing your name.""", false);
                }
            }
            // Reaction to regular messages
            else {
                if (umessage.hasText() && Database.getWaitStatus(umessage.getChatId().toString()).equals(Constants.getStatus_WAIT())) {
                    removeMessage(umessage.getChatId(), Database.getMessageId(umessage.getChatId().toString()));
                    sendMessage(umessage.getChatId(), """
                            📤 Your message has been sent anonymously!
                            
                            💙 Please wait for a response from the administration!""", true);
                    Database.updWaitStatus(Constants.getStatus_STAY(), umessage.getChatId().toString());
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), "\uD83D\uDCE4 An anonymous message has been sent\\!\n" +
                            "\n" +
                            "\uD83D\uDCAC Text:\n>" + escapeMarkdownV2(umessage.getText()), umessage.getChatId());

                } else {
                    removeMessage(umessage.getChatId(), umessage.getMessageId().toString());
                }
            }
        }

        // Reacts to messages from the admin chat
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
                    if (umessage.getText().equalsIgnoreCase("ban")) {
                        if (Database.getBanned(chatId).equals(Constants.getBanned_BANNED())) {
                            sendMessage(chatIdAdm, """
                                    ❌ Error:
                                    
                                    🔴 The user is already banned\\!""");
                        }
                        else {
                            Database.banUser(chatId);
                            sendMessage(chatIdAdm, """
                                    🔴 Ban:
                                    
                                    🚨 The user has been banned\\!""");
                            sendMessage(Long.parseLong(chatId), """
                                🔴 Ban:
                                
                                🚨 You have been banned!""", false);
                        }
                    }
                    else if (umessage.getText().equalsIgnoreCase("unban")) {
                        if (Database.getBanned(chatId).equals(Constants.getBanned_BANNED())) {
                            Database.pardonUser(chatId);
                            sendMessage(chatIdAdm, """
                                    🟢 Unban:
                                    
                                    ✅ The user has been unbanned\\!""");
                            sendMessage(Long.parseLong(chatId), """
                                    🟢 Unban:
                                    
                                    ✅ You have been unbanned!""", true);
                        }

                        else {
                            Database.banUser(chatId);
                            sendMessage(chatIdAdm, """
                                    ❌ Error:
                                    
                                    🔴 The user is not banned\\!""");
                        }
                    }
                    else {
                        if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN())
                                || Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                            sendMessage(Long.parseLong(chatId), """
                                 📬 You have received a response\\!
                                \s
                                 💬 Text:\s
                                >""" + escapeMarkdownV2(umessage.getText()));
                            sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), "\uD83D\uDCE4 An anonymous message has been sent\\!");
                        } else {
                            sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                            ❌ Error:
                            
                            🔴 Only administrators can respond\\!""");
                        }

                    }
                }
                else if (umessage.getText().equalsIgnoreCase("unban")
                        || umessage.getText().equalsIgnoreCase("ban")) {
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                            ❌ Error:
                            
                            🔴 Please reply to an anonymous message from the bot\\!""");
                }
                else if (umessage.isReply()
                        || umessage.getText().equalsIgnoreCase("promote")
                        && umessage.getText().equalsIgnoreCase("demote")) {
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                ❌ Error:
                                
                                🔴 This command cannot be used on the bot\\!""");
                }
            }
            else if (umessage.isReply() && !umessage.getReplyToMessage().getFrom().getIsBot() && umessage.hasText()) {
                long chatIdAdm = Long.parseLong(Constants.ADMIN_CHAT_ID);
                if (umessage.getText().equalsIgnoreCase("promote")) {
                    if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                            Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                    umessage.getReplyToMessage().getFrom().getUserName().equals(umessage.getFrom().getUserName())) {
                        sendMessage(chatIdAdm, """
                                ❌ Error:
                                
                                🔴 You cannot promote yourself\\!""");
                    }
                    else if(!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                        Database.addPermUser(umessage.getFrom().getUserName());
                        sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                ❌ Error:
                                
                                🔴 You are not a super admin\\!""");
                    }

                    else if (Database.getPerm(umessage.getReplyToMessage().getFrom().getUserName()).equals(Constants.getPerm_ADMIN())) {
                        sendMessage(chatIdAdm, """
                                ❌ Error:
                                
                                🔴 The user is already an administrator\\!""");
                    }
                    else {
                        Database.upgradePermUser(umessage.getReplyToMessage().getFrom().getUserName());
                        sendMessage(chatIdAdm, """
                                🟢 Permission change:
                                
                                ✅ The user has been successfully promoted\\!""");
                    }
                }
                else if(umessage.getText().equalsIgnoreCase("demote")) {
                    if(Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                            Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                    umessage.getReplyToMessage().getFrom().getUserName().equals(umessage.getFrom().getUserName())) {
                        sendMessage(chatIdAdm, """
                                ❌ Error:
                                
                                🔴 You cannot demote yourself\\!""");
                    }
                    else if(!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                        Database.addPermUser(umessage.getFrom().getUserName());
                        sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                ❌ Error:
                                
                                🔴 You are not a super admin\\!""");
                    }

                    else if (!Database.getPerm(umessage.getReplyToMessage().getFrom().getUserName()).equals(Constants.getPerm_ADMIN())) {
                        sendMessage(chatIdAdm, """
                                ❌ Error:
                                
                                🔴 The user is not an administrator\\!""");
                    }
                    else {
                        Database.demotePermUser(umessage.getReplyToMessage().getFrom().getUserName());
                        sendMessage(chatIdAdm, """
                                🟢 Permission change:
                                
                                ✅ The user has been successfully demoted\\!""");
                    }
                }
                else if (umessage.getText().equalsIgnoreCase("unban")
                        || umessage.getText().equalsIgnoreCase("ban")) {
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                ❌ Error:
                                
                                🔴 Please reply to an anonymous message from the bot\\!""");
                }
                else if (umessage.isReply() && umessage.getText().equalsIgnoreCase("whoyou")) {
                    sendMessage(chatIdAdm, "\uD83E\uDD16 User information: \n\n"
                            + "\uD83D\uDC64 Name: @" + umessage.getReplyToMessage().getFrom().getUserName()
                            +"\n\uD83D\uDD39 Permission level: " + parsePerm(Database.getPerm(umessage.getReplyToMessage().getFrom().getUserName())));
                }
            }
            else if (umessage.hasText() && !umessage.isReply() && umessage.getText().equalsIgnoreCase("whoi")) {
                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), "\uD83E\uDD16 User information: \n\n"
                        + "\uD83D\uDC64 Name: @" + umessage.getFrom().getUserName()
                        +"\n\uD83D\uDD39 Permission level: " + parsePerm(Database.getPerm(umessage.getFrom().getUserName())));
            }
            else if (umessage.hasText() && !umessage.isReply() && umessage.getText().equalsIgnoreCase("commands")) {
                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                        📜 Available commands:
                        
                        🔹 ban \\<reply to an anonymous message\\> – ban the user, preventing them from sending messages\\.
                        
                        🟢 unban \\<reply to an anonymous message\\> – unban the user, restoring their access to messages\\.
                        
                        📈 promote \\<user\\_id\\> /  \\<reply to a message\\> – promote the user's access level\\.
                       
                        📉 demote \\<user\\_id\\> / \\<reply to a message\\> – demote the user's access level\\.
                        
                        👤 whoi – check your access level\\.
                        
                        🤖 whoyou – information about the user whose ID is specified or whose message was replied to\\.
                        
                        🚀 Use commands only as intended\\!""");
            }
            else if (umessage.hasText() && !umessage.isReply()) {
                long chatIdAdm = Long.parseLong(Constants.ADMIN_CHAT_ID);
                if (getUserId(umessage.getText()) != null) {
                    String userId = getUserId(umessage.getText());
                    if (Database.getPerm(getUserId(umessage.getText())) != null) {
                        if (umessage.getText().toLowerCase().startsWith("promote")) {
                            if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                                    Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                            userId.equals(umessage.getFrom().getUserName())) {
                                sendMessage(chatIdAdm, """
                                        ❌ Error:
                                        
                                        🔴 You cannot promote yourself\\!""");
                            } else if (!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                        ❌ Error:
                                        
                                        🔴 You are not a super admin\\!""");
                            } else if (Database.getPerm(userId).equals(Constants.getPerm_ADMIN())) {
                                sendMessage(chatIdAdm, """
                                        ❌ Error:
                                        
                                        🔴 The user is already an administrator\\!""");
                            } else {
                                Database.upgradePermUser(userId);
                                sendMessage(chatIdAdm, """
                                        🟢 Permission change:
                                        
                                        ✅ The user has been successfully promoted\\!""");
                            }
                        }
                        else if (umessage.getText().toLowerCase().startsWith("demote")) {
                            if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                                    Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                            userId.equals(umessage.getFrom().getUserName())) {
                                sendMessage(chatIdAdm, """
                                        ❌ Error:
                                        
                                        🔴 You cannot demote yourself\\!""");
                            } else if (!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), """
                                        ❌ Error:
                                        
                                        🔴 You are not a super admin\\!""");
                            } else if (!Database.getPerm(userId).equals(Constants.getPerm_ADMIN())) {
                                sendMessage(chatIdAdm, """
                                        ❌ Error:
                                        
                                        🔴 The user is not an administrator\\!""");
                            } else {
                                Database.demotePermUser(userId);
                                sendMessage(chatIdAdm, """
                                        🟢 Permission change:
                                        
                                        ✅ The user has been successfully demoted\\!""");
                            }
                        }
                        else if (umessage.getText().toLowerCase().startsWith("whoyou")) {
                            sendMessage(chatIdAdm, "\uD83E\uDD16 User information: \n\n"
                                    + "\uD83D\uDC64 Name: @" + userId
                                    +"\n\uD83D\uDD39 Permission level: " + parsePerm(Database.getPerm(userId)));
                        }
                    }
                    else {
                        sendMessage(chatIdAdm, """
                                ❌ Error:
                                
                                🔴 The user is not in the database\\!""");
                    }
                }
                else if (umessage.hasText() && umessage.getText().toLowerCase().startsWith("promote") || umessage.getText().toLowerCase().startsWith("demote")){
                    sendMessage(chatIdAdm, """
                                ❌ Error:
                                
                                🔴 Please specify the username\\!""");
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
        row.add(new KeyboardButton("\uD83D\uDCE9 Send Message"));
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
