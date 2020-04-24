package de.timmi6790.statsbotdiscord.modules.core.commands.info;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.core.UserDb;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class AccountDeletionCommand extends AbstractCommand {
    private static final String[] RANDOM_CONFIRM_PHRASES = new String[]{"TakeMeBackToParadiseCity"};

    private final Cache<Long, Integer> userDeleteConfirmCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    // Ban the player from the bot when he is using more than 5 deletes per month/Bot uptime.
    // I think it is ok to give everyone the chance to delete their data without a long process,
    // but I hate it when people would abuse it, because it is incrementing the user ids
    private final LoadingCache<Long, Byte> deletionAbuseCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.DAYS)
            .build(id -> (byte) 0);

    public AccountDeletionCommand() {
        super("deleteMyAccount", "Info", "Wipe all my data!", "");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final Integer confirmPhraseId = this.userDeleteConfirmCache.getIfPresent(commandParameters.getUserDb().getDiscordId());
        if (confirmPhraseId == null) {
            final int phraseId = ThreadLocalRandom.current().nextInt(RANDOM_CONFIRM_PHRASES.length);
            final String phrase = RANDOM_CONFIRM_PHRASES[phraseId];

            this.userDeleteConfirmCache.put(commandParameters.getUserDb().getDiscordId(), phraseId);
            this.sendTimedMessage(commandParameters,
                    UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                            .setTitle("How to delete my account")
                            .setDescription("If you really wish to delete your account, write the " + MarkdownUtil.monospace("Confirm Command") + " in the next 5 minutes.\n" +
                                    MarkdownUtil.bold("THERE IS NO WAY TO REVERT THIS ACTION"))
                            .addField("Confirm Command", StatsBot.getCommandManager().getMainCommand() + " deleteMyAccount " + phrase, false),
                    300
            );

            return CommandResult.SUCCESS;
        }

        final String phrase = RANDOM_CONFIRM_PHRASES[confirmPhraseId];
        final String arg = commandParameters.getArgs().length != 0 ? commandParameters.getArgs()[0] : " ";
        if (!arg.equalsIgnoreCase(phrase)) {
            this.sendTimedMessage(commandParameters,
                    UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                            .setTitle("Incorrect confirm phrase")
                            .setDescription(MarkdownUtil.monospace(arg) + " is not your confirm phrase!\n" +
                                    "Please use the command in " + MarkdownUtil.monospace("Confirm Command") + " to delete your account")
                            .addField("Confirm Command", StatsBot.getCommandManager().getMainCommand() + " deleteMyAccount " + phrase, false),
                    90
            );

            return CommandResult.INVALID_ARGS;
        }

        final long userId = commandParameters.getUserDb().getDiscordId();
        byte abuseCounter = this.deletionAbuseCache.get(userId);
        if (abuseCounter >= 5) {
            commandParameters.getUserDb().ban(commandParameters, "AccountDeletionCommand abuse.");
            return CommandResult.SUCCESS;
        }

        this.sendTimedMessage(commandParameters,
                UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                        .setTitle("Bye")
                        .setDescription("It is sad to see you go USER_NAME, your data should be deleted in the next few seconds!"),
                90
        );

        abuseCounter++;
        this.deletionAbuseCache.put(commandParameters.getUserDb().getDiscordId(), abuseCounter);

        this.userDeleteConfirmCache.invalidate(userId);
        UserDb.getUSER_CACHE().invalidate(userId);
        StatsBot.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM player WHERE id = :dbId LIMIT 1;")
                        .bind("dbId", commandParameters.getUserDb().getDatabaseId())
                        .execute()
        );

        return CommandResult.SUCCESS;
    }
}
