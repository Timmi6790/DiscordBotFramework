ALTER TABLE `command_log`
    DROP FOREIGN KEY `command_log_command_id`;
ALTER TABLE `command_log`
    ADD CONSTRAINT `command_log_command_id_new` FOREIGN KEY (`command_id`) REFERENCES `command` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;