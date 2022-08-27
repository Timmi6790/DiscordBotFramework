package de.timmi6790.discord_framework.module.modules.rank.options;

import de.timmi6790.discord_framework.module.modules.rank.Rank;
import de.timmi6790.discord_framework.module.modules.rank.RankModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.option.Option;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Optional;

public class RankOption extends Option<Rank> {
    private final RankModule module;

    public RankOption(final String name, final String description, final RankModule module) {
        super(name, description, OptionType.STRING);

        this.module = module;
    }

    @Override
    public String convertToOption(final Rank option) {
        return option.getRankName();
    }

    @Override
    public Optional<Rank> convertValue(final OptionMapping mapping) {
        return this.module.getRank(mapping.getAsString());
    }
}
