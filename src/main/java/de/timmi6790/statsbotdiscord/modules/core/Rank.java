package de.timmi6790.statsbotdiscord.modules.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class Rank {
    private String name;
    private List<String> perms;
}
