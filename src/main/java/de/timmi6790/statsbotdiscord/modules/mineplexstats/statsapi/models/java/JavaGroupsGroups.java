package de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.java;

import de.timmi6790.statsbotdiscord.modules.mineplexstats.statsapi.models.ResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class JavaGroupsGroups extends ResponseModel {
    private final Map<String, JavaGroup> groups;
}
