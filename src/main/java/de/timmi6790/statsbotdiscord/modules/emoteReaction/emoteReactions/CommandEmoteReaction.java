package de.timmi6790.statsbotdiscord.modules.emoteReaction.emoteReactions;

import de.timmi6790.statsbotdiscord.modules.command.AbstractCommand;
import de.timmi6790.statsbotdiscord.modules.command.CommandParameters;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
public class CommandEmoteReaction extends AbstractEmoteReaction {
    // TODO: Find a better way, it is stupid to save the entire commandParameter object in here

    private final AbstractCommand command;
    private final CommandParameters commandParameters;

    @Override
    public void onEmote() {
        this.command.runCommand(this.commandParameters);
    }
}
