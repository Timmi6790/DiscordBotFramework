package de.timmi6790.discord_framework.module.modules.slashcommand.property.properties.info;

import de.timmi6790.discord_framework.module.modules.slashcommand.property.SlashCommandProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class DescriptionProperty implements SlashCommandProperty<String> {
    @NonNull
    private final String description;

    @Override
    public String getValue() {
        return this.description;
    }
}
