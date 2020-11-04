ALTER TABLE permission
    MODIFY COLUMN `permission_node` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL AFTER `id`;