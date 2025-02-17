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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient = new OkHttpTelegramClient(Constants.BOT_TOKEN);

    @Override
    public void consume(Update update) {
        // de-comment s out and start bot, send msg to adm chat and paste to constants
//        System.out.println(update.getMessage().getChatId());
        // Start message for the /start command
        if (update.hasMessage() && update.getMessage().getChat().isUserChat()) {
            Message umessage = update.getMessage();
            // Sends a start message + "Send Message" button

            if (umessage.hasText() && umessage.getText().equals("/start")) {
                Database.addUser(umessage.getChatId().toString());
                String startMessage = Constants.getStartMessage();
                sendMessage(umessage.getChatId(), startMessage, true);
            }
            // Reaction to pressing the "Send Message" button
            // Reaction to regular messages
            else {
                if (umessage.hasText() && umessage.getText().equals(Constants.getKeyboardMessage())) {
                    if (Database.getBanned(umessage.getChatId().toString()).equals(Constants.getBanned_BANNED())) {
                        removeMessage(umessage.getChatId(), umessage.getMessageId().toString());
                        sendMessage(umessage.getChatId(), Constants.getUserBannedMessage());
                    } else {
                        if (!Database.getWaitStatus(umessage.getChatId().toString()).equals(Constants.getStatus_WAIT())) {
                            removeMessage(umessage.getChatId(), umessage.getMessageId().toString());
                            sendMessage(umessage.getChatId(), Constants.getWaitMessage(), false);
                            Database.updWaitStatus(Constants.getStatus_WAIT(), umessage.getChatId().toString());
                        } else {
                            removeMessage(umessage.getChatId(), umessage.getMessageId().toString());
                        }
                    }
                }
                else if (umessage.hasText() && Database.getWaitStatus(umessage.getChatId().toString()).equals(Constants.getStatus_WAIT())) {
                    sendMessage(umessage.getChatId(), Constants.getUserFeedbackMessage(), true, umessage.getMessageId().toString());
                    Database.updWaitStatus(Constants.getStatus_STAY(), umessage.getChatId().toString());
                    removeMessage(umessage.getChatId(), Database.getMessageId(umessage.getChatId().toString()));
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getSentAdminMessage() + escapeMarkdownV2(umessage.getText()), umessage.getChatId());

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
                    if (umessage.getText().equalsIgnoreCase(Constants.getCommandBanMessage())) {
                        if (Database.getBanned(chatId).equals(Constants.getBanned_BANNED())) {
                            sendMessage(chatIdAdm, Constants.getUserAlreadyBannedMessage(), umessage.getMessageId().toString());
                        }
                        else {
                            Database.banUser(chatId);
                            sendMessage(chatIdAdm, Constants.getUserHasBeenBannedMessage(), umessage.getMessageId().toString());
                            sendMessage(Long.parseLong(chatId), Constants.getUserBannedFeedbackMessage(), false);
                        }
                    }
                    else if (umessage.getText().equalsIgnoreCase(Constants.getCommandUnbanMessage())) {
                        if (Database.getBanned(chatId).equals(Constants.getBanned_BANNED())) {
                            Database.pardonUser(chatId);
                            sendMessage(chatIdAdm, Constants.getUserHasUnbannedMessage(), umessage.getMessageId().toString());
                            sendMessage(Long.parseLong(chatId), Constants.getUserUnbannedFeedbackMessage(), true);
                        }

                        else {
                            Database.banUser(chatId);
                            sendMessage(chatIdAdm, Constants.getUserNotBannedMessage(), umessage.getMessageId().toString());
                        }
                    }
                    else {
                        if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN())
                                || Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                            sendMessage(Long.parseLong(chatId), Constants.getReceivedUserMessage() + escapeMarkdownV2(umessage.getText()));
                            sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getAdminFeedbackMessage(), umessage.getMessageId().toString());
                        } else {
                            sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getOnlyAdminsCanReplyMessage(), umessage.getMessageId().toString());
                        }

                    }
                }
                else if (!umessage.getText().equalsIgnoreCase(Constants.getCommandUnbanMessage())
                        || !umessage.getText().equalsIgnoreCase(Constants.getCommandBanMessage())) {
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getNotBotReplyMessage(), umessage.getMessageId().toString());
                }
                else if (umessage.isReply()
                        || umessage.getText().equalsIgnoreCase(Constants.getCommandPromoteMessage())
                        && umessage.getText().equalsIgnoreCase(Constants.getCommandDemoteMessage())) {
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getBotBannedCommandMessage(), umessage.getMessageId().toString());
                }
            }
            else if (umessage.isReply() && !umessage.getReplyToMessage().getFrom().getIsBot() && umessage.hasText()) {
                long chatIdAdm = Long.parseLong(Constants.ADMIN_CHAT_ID);
                if (umessage.getText().equalsIgnoreCase(Constants.getCommandPromoteMessage())) {
                    if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                            Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                    umessage.getReplyToMessage().getFrom().getUserName().equals(umessage.getFrom().getUserName())) {
                        sendMessage(chatIdAdm, Constants.getPromoteCantYourselfMessage(), umessage.getMessageId().toString());
                    }
                    else if(!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                        Database.addPermUser(umessage.getFrom().getUserName());
                        sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getPromoteNotSuperAdminMessage(), umessage.getMessageId().toString());
                    }

                    else if (Database.getPerm(umessage.getReplyToMessage().getFrom().getUserName()).equals(Constants.getPerm_ADMIN())) {
                        sendMessage(chatIdAdm, Constants.getPromoteAlreadyAdminMessage(), umessage.getMessageId().toString());
                    }
                    else {
                        Database.upgradePermUser(umessage.getReplyToMessage().getFrom().getUserName());
                        sendMessage(chatIdAdm, Constants.getPromoteSuccessMessage(), umessage.getMessageId().toString());
                    }
                }
                else if(umessage.getText().equalsIgnoreCase(Constants.getCommandDemoteMessage())) {
                    if(Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                            Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                    umessage.getReplyToMessage().getFrom().getUserName().equals(umessage.getFrom().getUserName())) {
                        sendMessage(chatIdAdm, Constants.getDemoteCantYourselfMessage(), umessage.getMessageId().toString());
                    }
                    else if(!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                        Database.addPermUser(umessage.getFrom().getUserName());
                        sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getDemoteNotSuperAdminMessage(), umessage.getMessageId().toString());
                    }

                    else if (!Database.getPerm(umessage.getReplyToMessage().getFrom().getUserName()).equals(Constants.getPerm_ADMIN())) {
                        sendMessage(chatIdAdm, Constants.getDemoteNotAdminMessage(), umessage.getMessageId().toString());
                    }
                    else {
                        Database.demotePermUser(umessage.getReplyToMessage().getFrom().getUserName());
                        sendMessage(chatIdAdm, Constants.getDemoteSuccessMessage(), umessage.getMessageId().toString());
                    }
                }
                else if (umessage.getText().equalsIgnoreCase(Constants.getCommandUnbanMessage())
                        || umessage.getText().equalsIgnoreCase(Constants.getCommandBanMessage())) {
                    sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getNotBotReplyMessage(), umessage.getMessageId().toString());
                }
                else if (umessage.isReply() && umessage.getText().equalsIgnoreCase(Constants.getCommandWhoYouMessage())) {
                    sendMessage(chatIdAdm, Constants.getCommandWhoInfoMessage()
                                           + Constants.getCommandWhoNameMessage()
                                           + umessage.getReplyToMessage().getFrom().getUserName()
                                           + Constants.getCommandWhoPermMessage()
                                           + parsePerm(Database.getPerm(umessage.getReplyToMessage().getFrom().getUserName())), umessage.getMessageId().toString());
                }
            }
            else if (umessage.hasText() && !umessage.isReply() && umessage.getText().equalsIgnoreCase(Constants.getCommandWhoIMessage())) {
                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getCommandWhoInfoMessage()
                                                                     + Constants.getCommandWhoNameMessage()
                                                                     + umessage.getFrom().getUserName()
                                                                     + Constants.getCommandWhoPermMessage()
                                                                     + parsePerm(Database.getPerm(umessage.getFrom().getUserName())), umessage.getMessageId().toString());
            }
            else if (umessage.hasText() && !umessage.isReply() && umessage.getText().equalsIgnoreCase(Constants.getCommandMessage())) {
                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getCommandsMessage(), umessage.getMessageId().toString());
            }
            else if (umessage.hasText() && !umessage.isReply()) {
                long chatIdAdm = Long.parseLong(Constants.ADMIN_CHAT_ID);
                if (getUserId(umessage.getText()) != null) {
                    String userId = getUserId(umessage.getText());
                    if (Database.getPerm(getUserId(umessage.getText())) != null) {
                        if (umessage.getText().toLowerCase().startsWith(Constants.getCommandPromoteMessage())) {
                            if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                                    Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                            userId.equals(umessage.getFrom().getUserName())) {
                                sendMessage(chatIdAdm, Constants.getPromoteCantYourselfMessage(), umessage.getMessageId().toString());
                            } else if (!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getPromoteNotSuperAdminMessage(), umessage.getMessageId().toString());
                            } else if (Database.getPerm(userId).equals(Constants.getPerm_ADMIN())) {
                                sendMessage(chatIdAdm, Constants.getPromoteAlreadyAdminMessage(), umessage.getMessageId().toString());
                            } else {
                                Database.upgradePermUser(userId);
                                sendMessage(chatIdAdm, Constants.getPromoteSuccessMessage(), umessage.getMessageId().toString());
                            }
                        } else if (umessage.getText().toLowerCase().startsWith(Constants.getCommandDemoteMessage())) {
                            if (Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_ADMIN()) ||
                                    Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN()) &&
                                            userId.equals(umessage.getFrom().getUserName())) {
                                sendMessage(chatIdAdm, Constants.getDemoteCantYourselfMessage(), umessage.getMessageId().toString());
                            } else if (!Database.getPerm(umessage.getFrom().getUserName()).equals(Constants.getPerm_SUPER_ADMIN())) {
                                sendMessage(Long.parseLong(Constants.ADMIN_CHAT_ID), Constants.getDemoteNotSuperAdminMessage());
                            } else if (!Database.getPerm(userId).equals(Constants.getPerm_ADMIN())) {
                                sendMessage(chatIdAdm, Constants.getDemoteNotAdminMessage(), umessage.getMessageId().toString());
                            } else {
                                Database.demotePermUser(userId);
                                sendMessage(chatIdAdm, Constants.getDemoteSuccessMessage(), umessage.getMessageId().toString());
                            }
                        } else if (umessage.getText().toLowerCase().startsWith(Constants.getCommandWhoYouMessage())) {
                            sendMessage(chatIdAdm, Constants.getCommandWhoInfoMessage()
                                    + Constants.getCommandWhoNameMessage() + userId
                                    + Constants.getCommandWhoPermMessage() + parsePerm(Database.getPerm(userId)), umessage.getMessageId().toString());
                        }
                    } else {
                        if (userId.equals(Constants.BOT_NAME)) {
                            sendMessage(chatIdAdm, Constants.getBotBannedCommandMessage(), umessage.getMessageId().toString());
                        } else {
                            sendMessage(chatIdAdm, Constants.getUserNotDatabaseMessage(), umessage.getMessageId().toString());
                        }
                    }
                }
                else if (umessage.hasText()
                         && umessage.getText().toLowerCase().startsWith(Constants.getCommandPromoteMessage())
                         || umessage.getText().toLowerCase().startsWith(Constants.getCommandDemoteMessage())
                         || umessage.getText().toLowerCase().startsWith(Constants.getCommandWhoYouMessage())) {
                    sendMessage(chatIdAdm, Constants.getSpecifyUsernameMessage(), umessage.getMessageId().toString());
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
                telegramClient.execute(message);
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
    public void sendMessage(long chat_id, String text, boolean keyboard, String message_id) {
        if (keyboard) {
            SendMessage message = SendMessage.builder()
                    .chatId(chat_id)
                    .text(text)
                    .replyToMessageId(Integer.valueOf(message_id))
                    .replyMarkup(getKeyboard())
                    .build();

            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace(System.err);
            }
        } else {
            SendMessage message = SendMessage.builder()
                    .chatId(chat_id)
                    .text(text)
                    .replyToMessageId(Integer.valueOf(message_id))
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
            Database.updGroupMessage(String.valueOf(chat_id_user), sentMessage.getMessageId().toString());;
        } catch (TelegramApiException e) {
                e.printStackTrace(System.err);
            }
        }
    public void sendMessage(long chat_id, String text, String message_id) {
        SendMessage message = SendMessage.builder()
                .chatId(chat_id)
                .text(text)
                .replyToMessageId(Integer.valueOf(message_id))
                .parseMode("MarkdownV2")
                .build();
        try {
            telegramClient.execute(message);
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
        row.add(new KeyboardButton(Constants.getKeyboardMessage()));
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
        if (Constants.LANGUAGE.equals("RU")) {
            return switch (perm) {
                case "USER" -> "Пользователь";
                case "ADMIN" -> "Админ";
                case "SUPER_ADMIN" -> "Супер\\-админ";
                default -> null;
            };
        } else {
            return switch (perm) {
                case "USER" -> "User";
                case "ADMIN" -> "Admin";
                case "SUPER_ADMIN" -> "Super\\-admin";
                default -> null;
            };
        }
    }
    public static String escapeMarkdownV2(String text) {
        return text.replaceAll("([\\\\_*()~`>#+\\-=|{}.!])", "\\\\$1");
    }
}
