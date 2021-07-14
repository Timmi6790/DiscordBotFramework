package de.timmi6790.discord_framework.module.modules.command.property.properties.info;

import de.timmi6790.discord_framework.module.modules.command.property.CommandProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class CategoryProperty implements CommandProperty<String> {
    @NonNull
    private final String category;

    @Override
    public String getValue() {
        return this.category;
    }
}
