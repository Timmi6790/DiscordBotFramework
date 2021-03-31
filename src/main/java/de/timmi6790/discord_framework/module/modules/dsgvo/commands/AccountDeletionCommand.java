package de.timmi6790.discord_framework.module.modules.dsgvo.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.discord_framework.module.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.module.modules.command.CommandParameters;
import de.timmi6790.discord_framework.module.modules.command.CommandResult;
import de.timmi6790.discord_framework.module.modules.dsgvo.DsgvoModule;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Account deletion command.
 */
@EqualsAndHashCode(callSuper = true)
public class AccountDeletionCommand extends AbstractCommand {
    /**
     * Command usage leading to a ban
     */
    private static final int BAN_THRESHOLD = 5;
    /**
     * The constant CONFIRM_COMMAND_NAME.
     */
    private static final String CONFIRM_COMMAND_NAME = "Confirm Command";

    /**
     * Delete confirm phrases
     */
    private static final String[] RANDOM_CONFIRM_PHRASES = new String[]{
            "PutMeDown",
            "SaveMeImBeingHeldCaptive",
            "GodSaveTheBot"
    };

    /**
     * The User delete confirm cache.
     */
    private final Cache<Long, Integer> userDeleteConfirmCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    /**
     * Ban the player from the bot when he is using more than 5 deletes per month/Bot uptime. I think it is ok to give
     * everyone the chance to delete their data without a long process, but I hate it when people would abuse it,
     * because it is incrementing the user ids
     */
    private final LoadingCache<Long, AtomicInteger> deletionAbuseCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.DAYS)
            .build(key -> new AtomicInteger(0));

    /**
     * The Dsgvo module.
     */
    private final DsgvoModule dsgvoModule;

    /**
     * Instantiates a new Account deletion command.
     */
    public AccountDeletionCommand(DsgvoModule dsgvoModule) {
        super(
                "deleteMyAccount",
                "Info",
                "Wipe all my data!",
                ""
        );

        this.dsgvoModule = dsgvoModule;
    }

    /**
     * Handle empty confirm phrase.
     *
     * @param commandParameters the command parameters
     * @return the command result
     */
    private CommandResult handleEmptyConfirmPhrase(final CommandParameters commandParameters) {
        final int phraseId = ThreadLocalRandom.current().nextInt(RANDOM_CONFIRM_PHRASES.length);
        final String phrase = RANDOM_CONFIRM_PHRASES[phraseId];

        this.userDeleteConfirmCache.put(commandParameters.getUserDb().getDiscordId(), phraseId);
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("How to delete my account")
                        .setDescription(
                                "If you really wish to delete your account, write the %s in "
                                        + "the next 5 minutes.%n%s",
                                MarkdownUtil.monospace(CONFIRM_COMMAND_NAME),
                                MarkdownUtil.bold("THERE IS NO WAY TO REVERT THIS ACTION")
                        )
                        .addField(
                                CONFIRM_COMMAND_NAME,
                                this.getCommandModule().getMainCommand() + " " + this.getName() + " " + phrase,
                                false
                        ),
                300
        );

        return CommandResult.SUCCESS;
    }

    /**
     * Send incorrect confirm phrase message.
     *
     * @param commandParameters the command parameters
     * @param userInput         the user input
     * @param phrase            the phrase
     */
    private void sendIncorrectConfirmPhraseMessage(final CommandParameters commandParameters,
                                                   final String userInput,
                                                   final String phrase) {
        this.sendTimedMessage(commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Incorrect confirm phrase")
                        .setDescription(
                                "%s is not your confirm phrase!\n" +
                                        "Please use the command in %s to delete your account",
                                MarkdownUtil.monospace(userInput),
                                MarkdownUtil.monospace(CONFIRM_COMMAND_NAME)
                        )
                        .addField(
                                CONFIRM_COMMAND_NAME,
                                this.getCommandModule().getMainCommand() + " " + this.getName() + " " + phrase,
                                false
                        )
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final long userId = commandParameters.getUserDb().getDiscordId();
        final Integer confirmPhraseId = this.userDeleteConfirmCache.getIfPresent(userId);
        // Check if the user has an existing phrase
        if (confirmPhraseId == null) {
            return this.handleEmptyConfirmPhrase(commandParameters);
        }

        final String requiredPhrase = RANDOM_CONFIRM_PHRASES[confirmPhraseId];
        final String userInput = this.getArgOrDefault(commandParameters, 0, " ");
        // Check if the user did input the correct phrase
        if (!userInput.equalsIgnoreCase(requiredPhrase)) {
            this.sendIncorrectConfirmPhraseMessage(commandParameters, userInput, requiredPhrase);
            return CommandResult.INVALID_ARGS;
        }

        // Check if we need to ban the user for abusing this command
        if (this.deletionAbuseCache.get(userId).getAndIncrement() >= BAN_THRESHOLD) {
            commandParameters.getUserDb().ban(commandParameters, "AccountDeletionCommand abuse.");
            return CommandResult.SUCCESS;
        }

        this.userDeleteConfirmCache.invalidate(userId);
        this.sendTimedMessage(
                commandParameters,
                "Bye",
                "It is sad to see you go USER_NAME, your data should be deleted in the next few seconds!"
        );

        // Let each module handle the delete themselves
        dsgvoModule.deleteUserData(commandParameters.getUserDb());
        
        return CommandResult.SUCCESS;
    }
}
