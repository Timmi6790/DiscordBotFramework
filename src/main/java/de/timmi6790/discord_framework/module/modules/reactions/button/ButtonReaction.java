package de.timmi6790.discord_framework.module.modules.reactions.button;

import de.timmi6790.discord_framework.module.modules.reactions.button.actions.ButtonAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class ButtonReaction {
    private final Map<String, ButtonAction> buttons = new HashMap<>();
    private final Set<Long> users = new HashSet<>();
    private final int deleteTime;
    private final boolean oneTimeUse;
    private final boolean deleteMessage;

    public ButtonReaction(final Map<Button, ButtonAction> buttons,
                          final Long userId,
                          final int deleteTime) {
        this(buttons, userId, deleteTime, true, true);
    }

    public ButtonReaction(final Map<Button, ButtonAction> buttons,
                          final Long userId) {
        this(buttons, userId, 300, true, true);
    }

    public ButtonReaction(@NonNull final Map<Button, ButtonAction> buttons,
                          final Long userId,
                          final int deleteTime,
                          final boolean oneTimeUse,
                          final boolean deleteMessage) {
        for (final Map.Entry<Button, ButtonAction> entry : buttons.entrySet()) {
            this.buttons.put(entry.getKey().getId(), entry.getValue());
        }

        this.users.add(userId);
        this.deleteTime = deleteTime;
        this.oneTimeUse = oneTimeUse;
        this.deleteMessage = deleteMessage;
    }
}
