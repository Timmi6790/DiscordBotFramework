package de.timmi6790.statsbotdiscord.modules.mineplexstats.commands.java;

import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.MineplexStatsModule;
import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java.JavaGroup;
import de.timmi6790.statsbotdiscord.utilities.UtilitiesDiscord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class JavaGroupsGroupsCommand extends AbstractJavaStatsCommand {
    public JavaGroupsGroupsCommand() {
        super("groups", "Groups", "[group]");

        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final MineplexStatsModule module = this.getStatsModule();

        // Show all groups
        if (commandParameters.getArgs().length == 0) {
            final EmbedBuilder message = UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                    .setTitle("Java Groups");

            final List<JavaGroup> groups = new ArrayList<>(module.getJavaGroups().values());
            groups.sort(Comparator.comparing(JavaGroup::getName));

            final StringJoiner fieldGroups = new StringJoiner("\n");
            for (final JavaGroup group : groups) {
                fieldGroups.add(group.getName());
            }
            message.addField("Groups", fieldGroups.toString(), false);

            commandParameters.getDiscordChannel().sendMessage(
                    message.build())
                    .delay(150, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();

            return CommandResult.SUCCESS;
        }

        // Group info
        final JavaGroup group = this.getJavaGroup(commandParameters, 0);
        final EmbedBuilder message = UtilitiesDiscord.getDefaultEmbedBuilder(commandParameters)
                .setTitle("Java Groups - " + group.getName())
                .addField("Description", group.getDescription(), false);

        if (group.getAliasNames().length > 0) {
            message.addField("Alias names", String.join(", ", group.getAliasNames()), false);
        }

        message.addField("Games", String.join(", ", group.getGameNames()), false);
        message.addField("Stats", String.join(", ", group.getStatNames()), false);

        commandParameters.getDiscordChannel().sendMessage(message.build())
                .delay(150, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue();

        return CommandResult.SUCCESS;
    }
}
