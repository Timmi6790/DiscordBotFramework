CREATE TABLE `achievement`
(
    `id`               int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `achievement_name` varchar(50)      NOT NULL,
    `hidden`           bit(1)           NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `achievement_achievement_name` (`achievement_name`) USING BTREE
);

CREATE TABLE `channel`
(
    `id`            int(11) UNSIGNED    NOT NULL AUTO_INCREMENT,
    `discordId`     bigint(50) UNSIGNED NOT NULL,
    `guild_id`      int(64) UNSIGNED    NOT NULL,
    `disabled`      bit(1)              NOT NULL DEFAULT b'0',
    `register_date` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `channel_discordId` (`discordId`) USING HASH
);

CREATE TABLE `command`
(
    `id`           int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `command_name` varchar(50)      NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `command_command_name` (`command_name`) USING HASH
);

CREATE TABLE `command_cause`
(
    `id`         int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `cause_name` varchar(50)      NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `command_cause_command_name` (`cause_name`) USING BTREE
);

CREATE TABLE `command_log`
(
    `id`                int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `command_id`        int(11) UNSIGNED NOT NULL,
    `command_cause_id`  int(11) UNSIGNED NOT NULL,
    `command_status_id` int(11) UNSIGNED NOT NULL,
    `in_guild`          bit(1)           NOT NULL,
    `date`              datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `command_log_date` (`date`) USING BTREE
);

CREATE TABLE `command_status`
(
    `id`          int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `status_name` varchar(50)      NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `command_status_status_name` (`status_name`) USING HASH
);

CREATE TABLE `guild`
(
    `id`            int(11) UNSIGNED    NOT NULL AUTO_INCREMENT,
    `discordId`     bigint(64) UNSIGNED NOT NULL,
    `banned`        bit(1)              NOT NULL DEFAULT b'0',
    `register_date` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `server_discordId` (`discordId`) USING HASH
);

CREATE TABLE `guild_command_alias`
(
    `id`       int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `guild_id` int(11) UNSIGNED NOT NULL,
    `alias`    varchar(10)      NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `guild_setting`
(
    `id`         int UNSIGNED     NOT NULL AUTO_INCREMENT,
    `guild_id`   int(11) UNSIGNED NOT NULL,
    `setting_id` int(11) UNSIGNED NOT NULL,
    `setting`    varchar(255)     NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `permission`
(
    `id`                 int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `permission_node`    varchar(50)      NOT NULL,
    `default_permission` bit(1)           NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `permission_permission_node` (`permission_node`) USING BTREE
);

CREATE TABLE `player`
(
    `id`            int(11) UNSIGNED    NOT NULL AUTO_INCREMENT,
    `discordId`     bigint(64) UNSIGNED NOT NULL,
    `primary_rank`  int(11) UNSIGNED    NOT NULL DEFAULT 1,
    `shop_points`   bigint(64) UNSIGNED NOT NULL DEFAULT 0,
    `banned`        bit(1)              NOT NULL DEFAULT b'0',
    `register_date` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `user_discordId` (`discordId`) USING HASH
);

CREATE TABLE `player_achievement`
(
    `id`             int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `player_id`      int(11) UNSIGNED NOT NULL,
    `achievement_id` int(11) UNSIGNED NOT NULL,
    `date`           datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `player_achievement_player_id_achievement_id` (`player_id`, `achievement_id`) USING BTREE
);

CREATE TABLE `player_permission`
(
    `id`            int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `player_id`     int(11) UNSIGNED NOT NULL,
    `permission_id` int(11) UNSIGNED NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `player_rank`
(
    `id`        int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `player_id` int(11) UNSIGNED NOT NULL,
    `rank_id`   int(11) UNSIGNED NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `player_setting`
(
    `id`         int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `player_id`  int(11) UNSIGNED NOT NULL,
    `setting_id` int(11) UNSIGNED NOT NULL,
    `setting`    varchar(255)     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `player_setting_player_id_setting_id` (`player_id`, `setting_id`) USING HASH
);

CREATE TABLE `player_stat`
(
    `id`        int(11) UNSIGNED    NOT NULL AUTO_INCREMENT,
    `player_id` int(11) UNSIGNED    NOT NULL,
    `stat_id`   int(11) UNSIGNED    NOT NULL,
    `value`     bigint(64) UNSIGNED NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `player_stat_player_id_stat_id` (`player_id`, `stat_id`) USING BTREE
);

CREATE TABLE `rank`
(
    `id`        int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `rank_name` varchar(50)      NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `rank_rank_name` (`rank_name`) USING BTREE
);

CREATE TABLE `rank_permission`
(
    `id`            int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `rank_id`       int(11) UNSIGNED NOT NULL,
    `permission_id` int(11) UNSIGNED NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rank_relation`
(
    `id`             int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `parent_rank_id` int(11) UNSIGNED NOT NULL,
    `child_rank_id`  int(11) UNSIGNED NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `rank_relation-parent_rank_id-child_rank_id` (`parent_rank_id`, `child_rank_id`) USING BTREE
);

CREATE TABLE `setting`
(
    `id`           int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `setting_name` varchar(50)      NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `setting_setting_name` (`setting_name`) USING BTREE
);

CREATE TABLE `setting_log`
(
    `id`          int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `setting_id`  int(11) UNSIGNED NOT NULL,
    `guild_id`    int(11) UNSIGNED NOT NULL,
    `player_id`   int(11) UNSIGNED NOT NULL,
    `new_setting` varchar(255)     NULL,
    `date`        datetime         NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `stat`
(
    `id`        int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `stat_name` varchar(50)      NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `stat_stat_stat_name` (`stat_name`) USING HASH
);

ALTER TABLE `channel`
    ADD CONSTRAINT `channel_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `command_log`
    ADD CONSTRAINT `command_log_command_status_id` FOREIGN KEY (`command_status_id`) REFERENCES `command_status` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `command_log`
    ADD CONSTRAINT `command_log_command_id` FOREIGN KEY (`command_status_id`) REFERENCES `command` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `command_log`
    ADD CONSTRAINT `command_log_command_cause` FOREIGN KEY (`command_cause_id`) REFERENCES `command_cause` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `guild_command_alias`
    ADD CONSTRAINT `guild_command_alias_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `guild_setting`
    ADD CONSTRAINT `guild_setting_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `guild_setting`
    ADD CONSTRAINT `guild_setting_setting_id` FOREIGN KEY (`setting_id`) REFERENCES `setting` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player`
    ADD CONSTRAINT `player_primary_rank` FOREIGN KEY (`primary_rank`) REFERENCES `rank` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player_achievement`
    ADD CONSTRAINT `player_achievement_player_id` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player_achievement`
    ADD CONSTRAINT `player_achievement_achievement_id` FOREIGN KEY (`achievement_id`) REFERENCES `achievement` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player_permission`
    ADD CONSTRAINT `player_permissions_player_id` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player_permission`
    ADD CONSTRAINT `player_permissions_permission_id` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player_rank`
    ADD CONSTRAINT `player_rank_player_id` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player_rank`
    ADD CONSTRAINT `player_rank_rank_id` FOREIGN KEY (`rank_id`) REFERENCES `rank` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player_setting`
    ADD CONSTRAINT `player_setting_player_id` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player_setting`
    ADD CONSTRAINT `player_setting_setting_id` FOREIGN KEY (`setting_id`) REFERENCES `setting` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player_stat`
    ADD CONSTRAINT `player_stat_player_id` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE NO ACTION ON UPDATE CASCADE;
ALTER TABLE `player_stat`
    ADD CONSTRAINT `player_stat_stat_id` FOREIGN KEY (`stat_id`) REFERENCES `stat` (`id`) ON DELETE NO ACTION ON UPDATE CASCADE;
ALTER TABLE `rank_permission`
    ADD CONSTRAINT `rank_permission_rank_id` FOREIGN KEY (`rank_id`) REFERENCES `rank` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `rank_permission`
    ADD CONSTRAINT `rank_permission_permission_id` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `rank_relation`
    ADD CONSTRAINT `rank_relation-parent_rank_id` FOREIGN KEY (`parent_rank_id`) REFERENCES `rank` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `rank_relation`
    ADD CONSTRAINT `rank_relation-child_rank_id` FOREIGN KEY (`child_rank_id`) REFERENCES `rank` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `setting_log`
    ADD CONSTRAINT `setting_log_setting_id` FOREIGN KEY (`setting_id`) REFERENCES `setting` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `setting_log`
    ADD CONSTRAINT `setting_log_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `setting_log`
    ADD CONSTRAINT `setting_log_player_id` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;

# Default Values
INSERT INTO `rank`(rank_name)
VALUES ('User');