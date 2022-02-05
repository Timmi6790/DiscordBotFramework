package de.timmi6790.discord_framework.module.modules.command.utilities;

import de.timmi6790.discord_framework.module.modules.command.models.CommandParameters;
import de.timmi6790.discord_framework.utilities.MultiEmbedBuilder;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.Set;
import java.util.StringJoiner;

@UtilityClass
public class MessageUtilities {
    public void sendUserBanMessage(final CommandParameters commandParameters) {
        commandParameters.sendPrivateMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("You are banned")
                        .setDescription("You are banned from using this bot.")
        );
    }

    public void sendGuildBanMessage(final CommandParameters commandParameters) {
        commandParameters.sendPrivateMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Banned Server")
                        .setDescription("This server is banned from using this bot.")
        );
    }

    public void sendMissingPermissionsMessage(final CommandParameters commandParameters) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Missing perms")
                        .setDescription("You don't have the permissions to run this command.")
        );
    }

    public void sendMissingDiscordPermissionMessage(final CommandParameters commandParameters,
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

    public void sendMissingArgsMessage(final CommandParameters commandParameters,
                                       final String syntax,
                                       final int requiredSyntaxLength,
                                       final String[] exampleCommands) {
        final String[] args = commandParameters.getArgs();
        final String[] splitSyntax = syntax.split(" ");

        final StringJoiner requiredSyntax = new StringJoiner(" ");
        for (int index = 0; Math.min(requiredSyntaxLength, splitSyntax.length) > index; index++) {
            requiredSyntax.add(args.length > index ? args[index] : MarkdownUtil.bold(splitSyntax[index]));
        }

        final String getFormattedExampleCommands = String.join("\n", exampleCommands);
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Missing Args")
                        .setDescription("You are missing a few required arguments.\n"
                                + "It is required that you enter the bold arguments.")
                        .addField("Required Syntax", requiredSyntax.toString(), false)
                        .addField("Command Syntax", syntax, false)
                        .addField("Example Commands", getFormattedExampleCommands, false, !getFormattedExampleCommands.isEmpty())
        );
    }

    public void sendErrorMessage(final CommandParameters commandParameters, final String description) {
        commandParameters.sendMessage(
                commandParameters.getEmbedBuilder()
                        .setTitle("Error")
                        .setDescription(description)
        );
    }

    public void sendInvalidArgumentMessage(final CommandParameters commandParameters,
                                           final String userInput,
                                           final String argumentName) {
        sendErrorMessage(
                commandParameters,
                MarkdownUtil.monospace(userInput) + " is not a valid " + MarkdownUtil.bold(argumentName) + " input."
        );
    }
}
