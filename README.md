# AnonymousChatBot
Anonymous chat bot for receiving and sending messages, suitable for small companies, gaming houses, flood chats, etc.

Features:

Sending messages from the user side, followed by forwarding to the admin group.

Ability to ban users without revealing their identity. (Requires using a command in response to an anonymous message from the bot)

Admin system: By default, the super admin is the person whose username is specified in the Constants File. When joining the group, a person receives the "USER" rank, which only allows reading messages, not responding. To promote a user, a command must be used. For more details about the bot's commands, type commands (write in the AdminChat).

The bot is distributed in two language settings: the English version is located in src/, while the folder ru/ contains the code with Russian message settings.

# Guide for get super-admin
1. Uncomment the System.out in the first lines of the code, add the bot to the admin chat, and send any message. The ChatId will appear in the console, which you need to insert into the Constants File along with the BOT_TOKEN and the super admin's username (without @).

2. Restart the bot, after which you will automatically become a super admin and will be able to promote or demote users.

3. Done! Tell your friends about the bot and start exchanging anonymous messages!
