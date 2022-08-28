package de.timmi6790.discord_framework.module.modules.dsgvo.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.timmi6790.discord_framework.module.modules.dsgvo.DsgvoModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommand;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.options.StringOption;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info.CategoryProperty;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.BaseCommandResult;
import de.timmi6790.discord_framework.module.modules.slashcommand.result.CommandResult;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Account deletion command.
 */
@EqualsAndHashCode(callSuper = true)
public class AccountDeletionCommand extends SlashCommand {
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

    private final Option<String> confirmPhraseOption = new StringOption("confirm-phase", "Delete confirm phrase");

    /**
     * Instantiates a new Account deletion command.
     */
    public AccountDeletionCommand(final DsgvoModule dsgvoModule,
                                  final SlashCommandModule module) {
        super(module, "deleteMyAccount", "Wipe all my data!");

        this.addProperties(
                new CategoryProperty("Info")
        );

        this.dsgvoModule = dsgvoModule;

        this.addOptions(
                this.confirmPhraseOption
        );
    }

    /**
     * Handle empty confirm phrase.
     *
     * @param commandParameters the command parameters
     * @return the command result
     */
    private CommandResult handleEmptyConfirmPhrase(final SlashCommandParameters commandParameters) {
        final int phraseId = ThreadLocalRandom.current().nextInt(RANDOM_CONFIRM_PHRASES.length);
        final String phrase = RANDOM_CONFIRM_PHRASES[phraseId];

        this.userDeleteConfirmCache.put(commandParameters.getUserDb().getDiscordId(), phraseId);
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("How to delete my account")
                        .setDescription(
                                "If you really wish to delete your account, write the %s in "
                                        + "the next 5 minutes.%n%s",
                                MarkdownUtil.monospace(CONFIRM_COMMAND_NAME),
                                MarkdownUtil.bold("THERE IS NO WAY TO REVERT THIS ACTION")
                        )
                        .addField(
                                CONFIRM_COMMAND_NAME,
                                "/" + this.getName() + " " + phrase,
                                false
                        )
        );

        return BaseCommandResult.SUCCESSFUL;
    }

    /**
     * Send incorrect confirm phrase message.
     *
     * @param commandParameters the command parameters
     * @param userInput         the user input
     * @param phrase            the phrase
     */
    private void sendIncorrectConfirmPhraseMessage(final SlashCommandParameters commandParameters,
                                                   final String userInput,
                                                   final String phrase) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Incorrect confirm phrase")
                        .setDescription(
                                "%s is not your confirm phrase!\n" +
                                        "Please use the command in %s to delete your account",
                                MarkdownUtil.monospace(userInput),
                                MarkdownUtil.monospace(CONFIRM_COMMAND_NAME)
                        )
                        .addField(
                                CONFIRM_COMMAND_NAME,
                                "/" + this.getName() + " " + phrase,
                                false
                        )
        );
    }

    @Override
    protected CommandResult onCommand(final SlashCommandParameters commandParameters) {
        final long userId = commandParameters.getUserDb().getDiscordId();
        final Integer confirmPhraseId = this.userDeleteConfirmCache.getIfPresent(userId);
        // Check if the user has an existing phrase
        if (confirmPhraseId == null) {
            return this.handleEmptyConfirmPhrase(commandParameters);
        }

        final String requiredPhrase = RANDOM_CONFIRM_PHRASES[confirmPhraseId];
        final String userInput = commandParameters.getOptionOrThrow(this.confirmPhraseOption);
        // Check if the user did input the correct phrase
        if (!userInput.equalsIgnoreCase(requiredPhrase)) {
            this.sendIncorrectConfirmPhraseMessage(commandParameters, userInput, requiredPhrase);
            return BaseCommandResult.INVALID_ARGS;
        }

        // Check if we need to ban the user for abusing this command
        if (this.deletionAbuseCache.get(userId).getAndIncrement() >= BAN_THRESHOLD) {
            commandParameters.getUserDb().ban(commandParameters, "AccountDeletionCommand abuse.");
            return BaseCommandResult.SUCCESSFUL;
        }

        this.userDeleteConfirmCache.invalidate(userId);
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Bye")
                        .setDescription("It is sad to see you go USER_NAME, your data should be deleted in the next few seconds!")
        );

        // Let each module handle the delete themselves
        this.dsgvoModule.deleteUserData(commandParameters.getUserDb());

        return BaseCommandResult.SUCCESSFUL;
    }
}
