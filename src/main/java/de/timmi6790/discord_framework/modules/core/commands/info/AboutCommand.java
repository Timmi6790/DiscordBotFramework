package de.timmi6790.discord_framework.modules.core.commands.info;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class AboutCommand extends AbstractCommand {
    public AboutCommand() {
        super("about", "Info", "About the bot", "");
        this.setDefaultPerms(true);
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        this.sendTimedMessage(
                commandParameters,
                this.getEmbedBuilder(commandParameters)
                        .setTitle("About")
                        .setDescription("This bot is themed around a minecraft server called [Mineplex](https://www.mineplex.com/home/).\n"
                                + "You can use it to show the game leaderboards or player stats based on the leaderboards.")
                        .addField("Data Collection", "The bot is only saving the minimal data to work, like the number of used commands, but not the actual command. "
                                + "If you want a copy of your data, you can request one in the contact formula. You can always delete all your saved data with the "
                                + MarkdownUtil.monospace(DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class).getMainCommand() + " deleteMyAccount") + " command. Abuse of this command could result in a ban, be warned.", false)
                        .addField("Contact Formula(Bugs, Exploits, Your Data)", "WIP", false)
                        .addField("SourceCode", "[StatsBotDiscord Github](https://github.com/Timmi6790/StatsBotDiscord)", false)
                        .addField("Version", DiscordBot.BOT_VERSION, false),
                150
        );
        return CommandResult.SUCCESS;
    }
}
