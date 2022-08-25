package de.timmi6790.discord_framework.module.modules.slashcommand.utilities;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandParameters;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Set;
import java.util.StringJoiner;

@UtilityClass
public class SlashMessageUtilities {
    public void sendUserBanMessage(final SlashCommandParameters commandParameters) {
        commandParameters.sendPrivateMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("You are banned")
                        .setDescription("You are banned from using this bot.")
        );
    }

    public void sendGuildBanMessage(final SlashCommandParameters commandParameters) {
        commandParameters.sendPrivateMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Banned Server")
                        .setDescription("This server is banned from using this bot.")
        );
    }

    public void sendMissingPermissionsMessage(final SlashCommandParameters commandParameters) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Missing perms")
                        .setDescription("You don't have the permissions to run this command.")
        );
    }

    public void sendMissingDiscordPermissionMessage(final SlashCommandParameters commandParameters,
                                                    final Set<Permission> missingPermissions) {
        final StringJoiner missingPerms = new StringJoiner(",");
        for (final Permission permission : missingPermissions) {
            missingPerms.add(MarkdownUtil.monospace(permission.getName()));
        }
        final MultiEmbedBuilder embedBuilder = commandParameters.getEmbedBuilder()
                .setTitle("Missing Discord Permission")
                .setDescription(
                        "The bot is missing %s permission(s).",
                        MarkdownUtil.monospace(missingPerms.toString())
                );

        // Only send it in the guild when we know that we have perms to do it
        if (missingPermissions.contains(Permission.MESSAGE_SEND)) {
            commandParameters.sendPrivateMessage(embedBuilder);
        } else {
            commandParameters.sendMessage(embedBuilder);
        }
    }

    public void sendErrorMessage(final SlashCommandParameters commandParameters, final String description) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Error")
                        .setDescription(description)
        );
    }

    public void sendInvalidArgumentMessage(final SlashCommandParameters commandParameters,
                                           final String userInput,
                                           final String argumentName) {
        sendErrorMessage(
                commandParameters,
                MarkdownUtil.monospace(userInput) + " is not a valid " + MarkdownUtil.bold(argumentName) + " input."
        );
    }
}
