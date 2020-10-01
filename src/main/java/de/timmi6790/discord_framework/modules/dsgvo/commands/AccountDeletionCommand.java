package de.timmi6790.discord_framework.modules.dsgvo.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.user.UserDbModule;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@EqualsAndHashCode(callSuper = true)
public class AccountDeletionCommand extends AbstractCommand {
    private static final String CONFIRM_COMMAND_NAME = "Confirm Command";

    private static final String[] RANDOM_CONFIRM_PHRASES = new String[]{"PutMeDown", "SaveMeImBeingHeldCaptive", "GodSaveTheBot"};

    private final Cache<Long, Integer> userDeleteConfirmCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    // Ban the player from the bot when he is using more than 5 deletes per month/Bot uptime.
    // I think it is ok to give everyone the chance to delete their data without a long process,
    // but I hate it when people would abuse it, because it is incrementing the user ids
    private final LoadingCache<Long, AtomicInteger> deletionAbuseCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.DAYS)
            .build(key -> new AtomicInteger(0));

    private final UserDbModule userDbModule;

    public AccountDeletionCommand() {
        super("deleteMyAccount", "Info", "Wipe all my data!", "");

        this.userDbModule = getModuleManager().getModuleOrThrow(UserDbModule.class);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final Integer confirmPhraseId = this.userDeleteConfirmCache.getIfPresent(commandParameters.getUserDb().getDiscordId());
        if (confirmPhraseId == null) {
            final int phraseId = ThreadLocalRandom.current().nextInt(RANDOM_CONFIRM_PHRASES.length);
            final String phrase = RANDOM_CONFIRM_PHRASES[phraseId];

            this.userDeleteConfirmCache.put(commandParameters.getUserDb().getDiscordId(), phraseId);
            sendTimedMessage(commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("How to delete my account")
                            .setDescription("If you really wish to delete your account, write the " + MarkdownUtil.monospace(CONFIRM_COMMAND_NAME) + " in the next 5 minutes.\n" +
                                    MarkdownUtil.bold("THERE IS NO WAY TO REVERT THIS ACTION"))
                            .addField(CONFIRM_COMMAND_NAME, this.getCommandModule().getMainCommand() + " deleteMyAccount " + phrase, false),
                    300
            );

            return CommandResult.SUCCESS;
        }

        final String phrase = RANDOM_CONFIRM_PHRASES[confirmPhraseId];
        final String arg = commandParameters.getArgs().length != 0 ? commandParameters.getArgs()[0] : " ";
        if (!arg.equalsIgnoreCase(phrase)) {
            sendTimedMessage(commandParameters,
                    getEmbedBuilder(commandParameters)
                            .setTitle("Incorrect confirm phrase")
                            .setDescription(MarkdownUtil.monospace(arg) + " is not your confirm phrase!\n" +
                                    "Please use the command in " + MarkdownUtil.monospace(CONFIRM_COMMAND_NAME) + " to delete your account")
                            .addField(CONFIRM_COMMAND_NAME, this.getCommandModule().getMainCommand() + " deleteMyAccount " + phrase, false),
                    90
            );

            return CommandResult.INVALID_ARGS;
        }

        final long userId = commandParameters.getUserDb().getDiscordId();
        if (this.deletionAbuseCache.get(userId).getAndIncrement() >= 5) {
            commandParameters.getUserDb().ban(commandParameters, "AccountDeletionCommand abuse.");
            return CommandResult.SUCCESS;
        }

        sendTimedMessage(commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Bye")
                        .setDescription("It is sad to see you go USER_NAME, your data should be deleted in the next few seconds!"),
                90
        );

        this.userDeleteConfirmCache.invalidate(userId);
        this.userDbModule.delete(commandParameters.getUserDb());

        return CommandResult.SUCCESS;
    }
}
