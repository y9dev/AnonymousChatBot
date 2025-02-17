package org.anonimchatbot;

public class Constants {

    // insert bot token from @BotFather
    public static final String BOT_TOKEN = "";

    // insert bot user without "@"
    public static final String BOT_NAME = "";

    // insert chat id for sent messages from users,
    // tip: can be get from update.getMessage().getChatId();
    public static final String ADMIN_CHAT_ID = "";

    // insert user login without @ for super-admin perms
    public static final String superAdmin_USER_ID = "";

    //insert RU or EN
    public static final String LANGUAGE = "RU";
    // constants getters for status
    public static String getStatus_WAIT() {
        return "WAIT";
    }
    public static String getStatus_STAY() {
        return "STAY";
    }
    public static String getBanned_BANNED() {
        return "BANNED";
    }
    public static String getPerm_USER() {
        return "USER";
    }
    public static String getPerm_ADMIN() {
        return "ADMIN";
    }
    public static String getPerm_SUPER_ADMIN() {
        return "SUPER_ADMIN";
    }

    public static String getStartMessage() {
        switch (LANGUAGE) {
            case "RU":
                return """
                        \uD83D\uDD35 Добро пожаловать в анонимный чат-бот!\s
                        
                        \uD83D\uDCAC Этот бот позволяет вам общаться с администрацей, оставаясь полностью анонимным.
                        
                        \uD83D\uDCE9 Как это работает?
                        
                        1️⃣ Отправьте сообщение — бот передаст его администрации.
                        
                        2️⃣ Получите ответ — переписка остаётся анонимной.
                        
                        \uD83D\uDE80 Начните анонимный чат с администрацией прямо сейчас!""";
            case "EN":
                return """
                        \uD83D\uDD35 Welcome to the anonymous chat bot!\s
                        
                        \uD83D\uDCAC This bot allows you to communicate with the administration while remaining completely anonymous.
                        
                        \uD83D\uDCE9 How does it work?
                        
                        1️⃣ Send a message — the bot will forward it to the administration.
                        
                        2️⃣ Receive a response — the conversation remains anonymous.
                        
                        \uD83D\uDE80 Start an anonymous chat with the administration right now!""";
            default: return null;
        }
    }
    public static String getWaitMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                            📝 Enter the text of your anonymous message...
                            
                            🔵 It will be sent to the administration without revealing your name.""";
            case "RU":
                return """
                            📝 Введите текст вашего анонимного сообщения...
                            
                            🔵 Оно будет отправлено администрации без указания вашего имени.""";
            default: return null;
        }
    }
    public static String getUserFeedbackMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                            📤 Your message has been sent anonymously!
                            
                            💙 Please wait for a response from the administration!""";
            case "RU":
                return """
                            📤 Ваше сообщение отправлено анонимно!
                            
                            💙 Ожидайте ответа от администрации!""";
            default: return null;
        }
    }
    public static String getAdminFeedbackMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          📤 An anonymous message has been sent\\!""";
            case "RU":
                return """
                          📤 Ответ отправлен\\!""";
            default: return null;
        }
    }
    public static String getSentAdminMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                        \uD83D\uDCE4 An anonymous message has been sent\\!
                        
                        \uD83D\uDCAC Text:
                        >""";
            case "RU":
                return """
                        \uD83D\uDCE4 Анонимное сообщение отправлено\\!
                        
                        \uD83D\uDCAC Текст:
                        >""";
                         
            default: return null;
        }
    }
    public static String getUserBannedMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                            ❌ Error:
                            
                            🔴 You are banned\\!""";
            case "RU":
                return """
                            ❌ Ошибка:
                            
                            🔴 Вы заблокированы\\!""";
            default: return null;
        }
    }
    public static String getUserAlreadyBannedMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                           ❌ Error:
                           
                           🔴 The user is already banned\\!""";
            case "RU":
                return """
                           ❌ Ошибка:
                           
                           🔴 Пользователь уже заблокирован\\!""";
            default: return null;
        }
    }
    public static String getUserHasBeenBannedMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                           🔴 Ban:
                         
                           🚨 The user has been banned\\!""";
            case "RU":
                return """
                           🔴 Блокировка:
                           
                           🚨 Пользователь заблокирован\\!""";
            default: return null;
        }
    }
    public static String getUserBannedFeedbackMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          🔴 Ban:
                          
                          🚨 You have been banned!""";
            case "RU":
                return """
                          🔴 Блокировка:
                          
                          🚨 Вы были заблокированы!""";
            default: return null;
        }
    }
    public static String getUserHasUnbannedMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          🟢 Unban:
                          
                          ✅ The user has been unbanned\\!""";
            case "RU":
                return """
                          🟢 Разблокировка:
                          
                          ✅ Пользователь разблокирован\\!""";
            default: return null;
        }
    }
    public static String getUserUnbannedFeedbackMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          🟢 Unban:
                          
                          ✅ You have been unbanned!""";
            case "RU":
                return """
                          🟢 Разблокировка:
                          
                          ✅ Вы были разблокированы!""";
            default: return null;
        }
    }
    public static String getReceivedUserMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          📬 You have received a response\\!
                          
                          💬 Text:
                          >""";
            case "RU":
                return """
                          📬 Вам пришел ответ\\!
                          
                          💬 Текст:
                          >""";
            default: return null;
        }
    }
    public static String getUserNotBannedMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                          
                          🔴 The user is not banned\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Пользователь не заблокирован\\!""";
            default: return null;
        }
    }
    public static String getOnlyAdminsCanReplyMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                          
                          🔴 Only administrators can respond\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Отвечать могут только администраторы\\!""";
            default: return null;
        }
    }
    public static String getNotBotReplyMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                          
                          🔴 Please reply to an anonymous message from the bot\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Отвечайте на анонимное сообщение от бота\\!""";
            default: return null;
        }
    }
    public static String getBotBannedCommandMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                          
                          🔴 This command cannot be used on the bot\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Команду нельзя использовать на боте\\!""";
            default: return null;
        }
    }
    public static String getPromoteCantYourselfMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                          
                          🔴 You cannot promote yourself\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                         
                          🔴 Вы не можете повысить сами себя\\!""";
            default: return null;
        }
    }
    public static String getPromoteNotSuperAdminMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                         
                          🔴 You are not a super admin\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Вы не являетесь супер админом\\!""";
            default: return null;
        }
    }
    public static String getPromoteAlreadyAdminMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                          
                          🔴 The user is already an administrator\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Пользователь уже является администратором\\!""";
            default: return null;
        }
    }
    public static String getPromoteSuccessMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          🟢 Permission change:
                          
                          ✅ The user has been successfully promoted\\!""";
            case "RU":
                return """
                          🟢 Изменение прав:
                         
                          ✅ Пользователь успешно повышен\\!""";
            default: return null;
        }
    }
    public static String getDemoteCantYourselfMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                          
                          🔴 You cannot demote yourself\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Вы не можете понизить сами себя\\!""";
            default: return null;
        }
    }
    public static String getDemoteNotSuperAdminMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                         
                          🔴 You are not a super admin\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Вы не являетесь супер админом\\!""";
            default: return null;
        }
    }
    public static String getDemoteNotAdminMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                          
                          🔴 The user is not an administrator\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Пользователь не является администратором\\!""";
            default: return null;
        }
    }
    public static String getDemoteSuccessMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          🟢 Permission change:
                          
                          ✅ The user has been successfully demoted\\!""";
            case "RU":
                return """
                          🟢 Изменение прав:
                          
                          ✅ Пользователь успешно понижен\\!""";
            default: return null;
        }
    }
    public static String getUserNotDatabaseMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                          
                          🔴 The user is not in the database\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Пользователя нет в базе данных\\!""";
            default: return null;
        }
    }
    public static String getSpecifyUsernameMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                          ❌ Error:
                          
                          🔴 Please specify the username\\!""";
            case "RU":
                return """
                          ❌ Ошибка:
                          
                          🔴 Напишите имя пользователя\\!""";
            default: return null;
        }
    }
    public static String getCommandsMessage() {
        switch (LANGUAGE) {
            case "EN":
                return """
                        📜 Available commands:
                        
                        🔹 ban \\<reply to an anonymous message\\> – ban the user, preventing them from sending messages\\.
                        
                        🟢 unban \\<reply to an anonymous message\\> – unban the user, restoring their access to messages\\.
                        
                        📈 promote \\<user\\_id\\> /  \\<reply to a message\\> – promote the user's access level\\.
                       
                        📉 demote \\<user\\_id\\> / \\<reply to a message\\> – demote the user's access level\\.
                        
                        👤 who i – check your access level\\.
                        
                        🤖 who you – information about the user whose ID is specified or whose message was replied to\\.
                        
                        🚀 Use commands only as intended\\!""";
            case "RU":
                return """
                        📜 Доступные команды:
                        
                        🔹 бан \\<с ответом на анонимное сообщение\\> – заблокировать пользователя, запретив ему отправлять сообщения\\.
                        
                        🟢 разбан \\<с ответом на анонимное сообщение\\> – разблокировать пользователя, вернув доступ к сообщениям\\.
                        
                        📈 повысить \\<user\\_id\\> /  \\<с ответом сообщение\\> – повысить уровень доступа пользователя\\.
                       
                        📉 понизить \\<user\\_id\\> / \\<с ответом сообщение\\> – понизить уровень доступа пользователя\\.
                        
                        👤 кто я – узнать свой уровень доступа\\.
                        
                        🤖 кто ты – информация о пользователе, чей ID указан или на чье сообщение был ответ\\.
                        
                        🚀 Используйте команды только по назначению\\!""";
            default: return null;
        }
    }
    public static String getCommandMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "commands";
            case "RU":
                return "команды";
            default: return null;
        }
    }
    public static String getCommandBanMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "ban";
            case "RU":
                return "бан";
            default: return null;
        }
    }
    public static String getCommandUnbanMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "unban";
            case "RU":
                return "разбан";
            default: return null;
        }
    }
    public static String getCommandDemoteMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "demote";
            case "RU":
                return "понизить";
            default: return null;
        }
    }
    public static String getCommandPromoteMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "promote";
            case "RU":
                return "повысить";
            default: return null;
        }
    }
    public static String getCommandWhoIMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "who i";
            case "RU":
                return "кто я";
            default: return null;
        }
    }
    public static String getCommandWhoYouMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "who you";
            case "RU":
                return "кто ты";
            default: return null;
        }
    }
    public static String getCommandWhoInfoMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "\uD83E\uDD16 User information: \n\n";
            case "RU":
                return "\uD83E\uDD16 Информация о пользователе: \n\n";
            default: return null;
        }
    }
    public static String getCommandWhoNameMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "\uD83D\uDC64 Name: @";
            case "RU":
                return "\uD83D\uDC64 Имя: @";
            default: return null;
        }
    }
    public static String getCommandWhoPermMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "\n\uD83D\uDD39 Permission level: ";
            case "RU":
                return "\n\uD83D\uDD39 Уровень прав: ";
            default: return null;
        }
    }
    public static String getKeyboardMessage() {
        switch (LANGUAGE) {
            case "EN":
                return "\uD83D\uDCE9 Send Message";
            case "RU":
                return "\uD83D\uDCE9 Отправить сообщение";
            default: return null;
        }
    }
}
