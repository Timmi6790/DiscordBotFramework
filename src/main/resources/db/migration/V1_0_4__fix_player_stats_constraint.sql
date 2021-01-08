SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `player_stat` DROP FOREIGN KEY `player_stat_player_id`;
ALTER TABLE `player_stat` DROP FOREIGN KEY `player_stat_stat_id`;

ALTER TABLE `player_stat`
    ADD CONSTRAINT `player_stat_player_id` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `player_stat`
    ADD CONSTRAINT `player_stat_stat_id` FOREIGN KEY (`stat_id`) REFERENCES `stat` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;

SET FOREIGN_KEY_CHECKS=1;