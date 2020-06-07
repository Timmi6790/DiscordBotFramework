package de.timmi6790.statsbotdiscord.modules.core.commands.management;

import de.timmi6790.statsbotdiscord.StatsBot;
import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import de.timmi6790.statsbotdiscord.modules.command.CommandResult;
import de.timmi6790.statsbotdiscord.modules.core.Rank;
import de.timmi6790.statsbotdiscord.modules.core.UserDb;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class UserInfoCommand extends AbstractCommand {
    public UserInfoCommand() {
        super("sinfo", "Management", "", "<discordUser>");

        this.setPermission("core.management.sinfo");
        this.setMinArgs(1);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final User user = this.getDiscordUser(commandParameters, 0);
        final UserDb userDb = UserDb.getOrCreate(user.getIdLong());

        final int commandSpamCache = StatsBot.getCommandManager().getCommandSpamCache().get(user.getIdLong()).get();
        final int activeEmotes = StatsBot.getEmoteReactionManager().getActiveEmotesPerPlayer().getOrDefault(user.getIdLong(), new AtomicInteger(0)).get();

        final String settings = userDb.getSettingsMap()
                .entrySet()
                .stream()
                .map(setting -> setting.getKey().getInternalName() + ": " + setting.getKey().parseSetting(setting.getValue()))
                .collect(Collectors.joining("\n"));

        final String stats = userDb.getStatsMap()
                .entrySet()
                .stream()
                .map(setting -> setting.getKey().getInternalName() + ": " + setting.getValue())
                .collect(Collectors.joining("\n"));

        final String subRanks = userDb.getRanks()
                .stream()
                .map(Rank::getName)
                .collect(Collectors.joining("; "));

        this.sendTimedMessage(commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("User Info")
                        .addField("Command Spam Cache", String.valueOf(commandSpamCache), true)
                        .addField("Active Emotes", String.valueOf(activeEmotes), true)
                        .addField("Shop Points", String.valueOf(userDb.getPoints()), false)
                        .addField("Ranks", userDb.getPrimaryRank() + "[" + subRanks + "]", true)
                        .addField("Settings", settings, false)
                        .addField("Stats", stats, false)
                        .addField("User Perms", String.join("\n", userDb.getPermissionNodes()), false),
                90
        );

        return CommandResult.SUCCESS;
    }
}
