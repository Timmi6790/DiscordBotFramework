package de.timmi6790.discord_framework.modules.database;

import lombok.Data;

/**
 * Database module config.
 */
@Data
public class Config {
    /**
     * The database url. Currently only supports mariadb
     */
    private String url = "jdbc:mariadb://127.0.0.1:3306/Database";
    /**
     * The database user.
     */
    private String name = "";
    /**
     * The database password for the given user.
     */
    private String password = "";
}
