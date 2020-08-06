package de.timmi6790.external_modules.mineplexstats.commands.java;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.ExampleCommandsCommandProperty;
import de.timmi6790.external_modules.mineplexstats.statsapi.models.java.JavaGroup;

import java.util.Comparator;
import java.util.stream.Collectors;

public class JavaGroupsGroupsCommand extends AbstractJavaStatsCommand {
    public JavaGroupsGroupsCommand() {
        super("groups", "Java Groups", "[group]");

        this.setCategory("MineplexStats - Java - Group");
        this.addProperties(
                new ExampleCommandsCommandProperty(
                        "MixedArcade"
                )
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        // Show all groups
        if (commandParameters.getArgs().length == 0) {
            this.sendTimedMessage(
                    commandParameters,
                    this.getEmbedBuilder(commandParameters)
                            .setTitle("Java Groups")
                            .addField("Groups",
                                    this.getStatsModule().getJavaGroups().values()
                                            .stream()
                                            .map(JavaGroup::getName)
                                            .sorted(Comparator.naturalOrder())
                                            .collect(Collectors.joining("\n")),
                                    false)
                            .setFooter("TIP: Run " + DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).getMainCommand() + " groups <group> to see more details"),
                    150
            );
            return CommandResult.SUCCESS;
        }

        // Group info
        final JavaGroup group = this.getJavaGroup(commandParameters, 0);
        // Remove "Achievement" from all stat names, because MixedArcade is above the 1024 character limit
        final String stats = group.getStatNames()
                .stream()
                .map(stat -> stat.replace(" ", "").replace("Achievement", ""))
                .collect(Collectors.joining(", "));

        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("Java Groups - " + group.getName())
                        .addField("Description", group.getDescription(), false, !group.getDescription().isEmpty())
                        .addField("Alias names", String.join(", ", group.getAliasNames()), false, group.getAliasNames().length > 0)
                        .addField("Games", String.join(", ", group.getGameNames()), false)
                        .addField("Stats", stats.substring(0, Math.min(stats.length(), 1024)), false),
                150
        );
        return CommandResult.SUCCESS;
    }
}
