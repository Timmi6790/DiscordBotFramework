DROP TABLE IF EXISTS guild_command_alias;

CREATE TABLE `guild_setting`  (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guild_id` int(11) UNSIGNED NOT NULL,
  `setting_id` int(11) UNSIGNED NOT NULL,
  `setting` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `guild_setting_guild_id_setting_id`(`guild_id`, `setting_id`) USING HASH
);

ALTER TABLE `guild_setting` ADD CONSTRAINT `guild_setting_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `guild_setting` ADD CONSTRAINT `guild_setting_setting_id` FOREIGN KEY (`setting_id`) REFERENCES `setting` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;