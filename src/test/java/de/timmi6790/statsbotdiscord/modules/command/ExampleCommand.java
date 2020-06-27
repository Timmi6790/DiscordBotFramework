package de.timmi6790.statsbotdiscord.modules.command;

public class ExampleCommand extends AbstractCommand {
    public ExampleCommand() {
        super(0, "Tets", "Tets", "Tets", "");
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        return null;
    }
}
