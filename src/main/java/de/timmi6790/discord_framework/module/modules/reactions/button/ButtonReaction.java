package de.timmi6790.discord_framework.module.modules.reactions.button;

import de.timmi6790.discord_framework.module.modules.reactions.button.actions.ButtonAction;
import de.timmi6790.discord_framework.module.modules.reactions.common.BaseReaction;
import lombok.*;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class ButtonReaction extends BaseReaction {
    private final Map<String, ButtonAction> buttons = new HashMap<>();

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
        super(Set.of(userId), deleteTime, oneTimeUse, deleteMessage);

        for (final Map.Entry<Button, ButtonAction> entry : buttons.entrySet()) {
            this.buttons.put(entry.getKey().getId(), entry.getValue());
        }
    }
}
