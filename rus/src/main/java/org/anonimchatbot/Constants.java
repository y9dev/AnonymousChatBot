package org.anonimchatbot;

public class Constants {

    // insert bot token from @BotFather
    public static final String BOT_TOKEN = "";

    // insert chat id for sent messages from users,
    // tip: can be get from update.getMessage().getChatId();
    public static final String ADMIN_CHAT_ID = "";

    // insert user login without @ for super-admin perms
    public static final String superAdmin_USER_ID = "";

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
    public static String getBanned_PARDONED() {
        return "PARDONED";
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

}
