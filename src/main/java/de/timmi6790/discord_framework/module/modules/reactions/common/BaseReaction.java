package de.timmi6790.discord_framework.module.modules.reactions.common;

import lombok.Data;

import java.util.Set;

@Data
public class BaseReaction {
    private final Set<Long> users;
    private final int deleteTime;
    private final boolean oneTimeUse;
    private final boolean deleteMessage;
}
