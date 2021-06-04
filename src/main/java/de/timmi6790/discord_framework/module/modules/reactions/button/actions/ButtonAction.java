package de.timmi6790.discord_framework.module.modules.reactions.button.actions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public interface ButtonAction {
    void onButtonClick(ButtonClickEvent buttonClickEvent);
}
