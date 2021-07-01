CREATE SCHEMA "channel";
CREATE SCHEMA "command";
CREATE SCHEMA "guild";
CREATE SCHEMA "rank";
CREATE SCHEMA "user";

CREATE TABLE "channel"."channels"
(
    "discord_id"    int8        NOT NULL,
    "guild_id"      int4        NOT NULL,
    "disabled"      bool        NOT NULL DEFAULT FALSE,
    "register_date" timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("discord_id")
);

CREATE TABLE "command"."command_causes"
(
    "id"         serial4,
    "cause_name" varchar(124) NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "command_causes-command_name_lower" ON "command"."command_causes" USING btree (
                                                                                                   LOWER(cause_name)
    );

CREATE TABLE "command"."command_logs"
(
    "id"                serial4,
    "command_id"        int4        NOT NULL,
    "command_cause_id"  int4        NOT NULL,
    "command_status_id" int4        NOT NULL,
    "in_guild"          bool        NOT NULL,
    "date"              timestamptz NOT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "command_logs-date" ON "command"."command_logs" USING btree (
                                                                          "date"
    );

CREATE TABLE "command"."command_status"
(
    "id"          serial4,
    "status_name" varchar(124) NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "command_status-status_name_lower" ON "command"."command_status" USING btree (
                                                                                                  LOWER(status_name)
    );

CREATE TABLE "command"."commands"
(
    "id"           serial4,
    "command_name" varchar(124) NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "commands-command_name_lower" ON "command"."commands" USING btree (
                                                                                       LOWER(command_name)
    );

CREATE TABLE "guild"."guild_settings"
(
    "id"         serial8,
    "guild_id"   int8 NOT NULL,
    "setting_id" int4 NOT NULL,
    "setting"    text NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "guild_settings-guild_id-setting_id" UNIQUE ("guild_id", "setting_id")
);

CREATE TABLE "guild"."guilds"
(
    "discord_id"    int8        NOT NULL,
    "banned"        bool        NOT NULL DEFAULT FALSE,
    "register_date" timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("discord_id")
);

CREATE TABLE "public"."achievements"
(
    "id"               serial4,
    "achievement_name" varchar(124) NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "achievements-achievement_name_lower" ON "public"."achievements" USING btree (
                                                                                                  LOWER(achievement_name)
    );

CREATE TABLE "public"."permissions"
(
    "id"              serial4,
    "permission_node" varchar(124) NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "permissions_permission_node" ON "public"."permissions" USING btree (
                                                                                         LOWER(permission_node)
    );

CREATE TABLE "public"."settings"
(
    "id"           serial4,
    "setting_name" varchar(124) NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "settings-setting_name_lower" ON "public"."settings" USING btree (
                                                                                      LOWER(setting_name)
    );

CREATE TABLE "public"."stats"
(
    "id"        serial4,
    "stat_name" varchar(124) NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "stats_stat_name" ON "public"."stats" USING btree (
                                                                       LOWER(stat_name)
    );

CREATE TABLE "rank"."rank_permissions"
(
    "id"            int4 NOT NULL,
    "rank_id"       int4 NOT NULL,
    "permission_id" int4 NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "rank_permissions-rank_id-permission_id" UNIQUE ("rank_id", "permission_id")
);

CREATE TABLE "rank"."rank_relations"
(
    "id"             serial4,
    "parent_rank_id" int4 NOT NULL,
    "child_rank_id"  int4 NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "rank_relations-parent_rank_id-child_rank_id" UNIQUE ("parent_rank_id", "child_rank_id")
);

CREATE TABLE "rank"."ranks"
(
    "id"        serial4,
    "rank_name" varchar(124) NOT NULL,
    PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX "rank_rank_name_lower" ON "rank"."ranks" USING btree (
                                                                          LOWER(rank_name)
    );

CREATE TABLE "user"."user_achievements"
(
    "id"             serial4,
    "user_id"        int8        NOT NULL,
    "achievement_id" int4        NOT NULL,
    "date"           timestamptz NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "user_achievements-user_id-achievement_id" UNIQUE ("user_id", "achievement_id")
);

CREATE TABLE "user"."user_permissions"
(
    "id"            serial4,
    "user_id"       int8 NOT NULL,
    "permission_id" int4 NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "user_permissions-user_id-permission_id" UNIQUE ("user_id", "permission_id")
);

CREATE TABLE "user"."user_ranks"
(
    "id"      serial4,
    "user_id" int8 NOT NULL,
    "rank_id" int4 NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "user_ranks-user_id-rank_id" UNIQUE ("user_id", "rank_id")
);

CREATE TABLE "user"."user_settings"
(
    "id"         serial4,
    "user_id"    int8 NOT NULL,
    "setting_id" int4 NOT NULL,
    "setting"    text NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "user_settings-user_id-setting_id" UNIQUE ("user_id", "setting_id")
);

CREATE TABLE "user"."user_stats"
(
    "id"      serial4,
    "user_id" int8 NOT NULL,
    "stat_id" int4 NOT NULL,
    "value"   int4 NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "user_stats-user_id-stat_id" UNIQUE ("user_id", "stat_id")
);

CREATE TABLE "user"."users"
(
    "discordId"     int8        NOT NULL,
    "primary_rank"  int4        NOT NULL,
    "banned"        bool        NOT NULL DEFAULT FALSE,
    "register_date" timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("discordId")
);

ALTER TABLE "channel"."channels"
    ADD CONSTRAINT "channels-guild_id" FOREIGN KEY ("guild_id") REFERENCES "guild"."guilds" ("discord_id");
DROP INDEX "command"."command_causes-command_name_lower";
CREATE UNIQUE INDEX "command_causes-command_name_lower" ON "command"."command_causes" USING btree (
                                                                                                   LOWER(cause_name)
    );
DROP INDEX "command"."command_logs-date";
ALTER TABLE "command"."command_logs"
    ADD CONSTRAINT "command_logs-command_status_id" FOREIGN KEY ("command_status_id") REFERENCES "command"."command_status" ("id");
ALTER TABLE "command"."command_logs"
    ADD CONSTRAINT "command_logs-command_id" FOREIGN KEY ("command_id") REFERENCES "command"."commands" ("id");
ALTER TABLE "command"."command_logs"
    ADD CONSTRAINT "command_logs-command_cause" FOREIGN KEY ("command_cause_id") REFERENCES "command"."command_causes" ("id");
CREATE INDEX "command_logs-date" ON "command"."command_logs" USING btree (
                                                                          "date"
    );
DROP INDEX "command"."command_status-status_name_lower";
CREATE UNIQUE INDEX "command_status-status_name_lower" ON "command"."command_status" USING btree (
                                                                                                  LOWER(status_name)
    );
DROP INDEX "command"."commands-command_name_lower";
CREATE UNIQUE INDEX "commands-command_name_lower" ON "command"."commands" USING btree (
                                                                                       LOWER(command_name)
    );
ALTER TABLE "guild"."guild_settings"
    ADD CONSTRAINT "guild_settings-guild_id" FOREIGN KEY ("guild_id") REFERENCES "guild"."guilds" ("discord_id");
ALTER TABLE "guild"."guild_settings"
    ADD CONSTRAINT "guild_settings-setting_id" FOREIGN KEY ("setting_id") REFERENCES "public"."settings" ("id");
DROP INDEX "public"."achievements-achievement_name_lower";
CREATE UNIQUE INDEX "achievements-achievement_name_lower" ON "public"."achievements" USING btree (
                                                                                                  LOWER(achievement_name)
    );
DROP INDEX "public"."permissions_permission_node";
CREATE UNIQUE INDEX "permissions_permission_node" ON "public"."permissions" USING btree (
                                                                                         LOWER(permission_node)
    );
DROP INDEX "public"."settings-setting_name_lower";
CREATE UNIQUE INDEX "settings-setting_name_lower" ON "public"."settings" USING btree (
                                                                                      LOWER(setting_name)
    );
DROP INDEX "public"."stats_stat_name";
CREATE UNIQUE INDEX "stats_stat_name" ON "public"."stats" USING btree (
                                                                       LOWER(stat_name)
    );
ALTER TABLE "rank"."rank_permissions"
    ADD CONSTRAINT "rank_permissions-rank_id" FOREIGN KEY ("rank_id") REFERENCES "rank"."ranks" ("id");
ALTER TABLE "rank"."rank_permissions"
    ADD CONSTRAINT "rank_permissions-permission_id" FOREIGN KEY ("permission_id") REFERENCES "public"."permissions" ("id");
ALTER TABLE "rank"."rank_relations"
    ADD CONSTRAINT "rank_relations-parent_rank_id" FOREIGN KEY ("parent_rank_id") REFERENCES "rank"."ranks" ("id");
ALTER TABLE "rank"."rank_relations"
    ADD CONSTRAINT "rank_relations-child_rank_id" FOREIGN KEY ("child_rank_id") REFERENCES "rank"."ranks" ("id");
DROP INDEX "rank"."rank_rank_name_lower";
CREATE UNIQUE INDEX "rank_rank_name_lower" ON "rank"."ranks" USING btree (
                                                                          LOWER(rank_name)
    );
ALTER TABLE "user"."user_achievements"
    ADD CONSTRAINT "user_achievements-user_id" FOREIGN KEY ("user_id") REFERENCES "user"."users" ("discordId");
ALTER TABLE "user"."user_achievements"
    ADD CONSTRAINT "user_achievements-achievement_id" FOREIGN KEY ("achievement_id") REFERENCES "public"."achievements" ("id");
ALTER TABLE "user"."user_permissions"
    ADD CONSTRAINT "user_permissions-user_id" FOREIGN KEY ("user_id") REFERENCES "user"."users" ("discordId");
ALTER TABLE "user"."user_permissions"
    ADD CONSTRAINT "user_permissions-permission_id" FOREIGN KEY ("permission_id") REFERENCES "public"."permissions" ("id");
ALTER TABLE "user"."user_ranks"
    ADD CONSTRAINT "user_ranks-user_id" FOREIGN KEY ("user_id") REFERENCES "user"."users" ("discordId");
ALTER TABLE "user"."user_ranks"
    ADD CONSTRAINT "user_ranks-rank_id" FOREIGN KEY ("rank_id") REFERENCES "rank"."ranks" ("id");
ALTER TABLE "user"."user_settings"
    ADD CONSTRAINT "user_settings-user_id" FOREIGN KEY ("user_id") REFERENCES "user"."users" ("discordId");
ALTER TABLE "user"."user_settings"
    ADD CONSTRAINT "user_settings-setting_id" FOREIGN KEY ("setting_id") REFERENCES "public"."settings" ("id");
ALTER TABLE "user"."user_stats"
    ADD CONSTRAINT "user_stats-user_id" FOREIGN KEY ("user_id") REFERENCES "user"."users" ("discordId");
ALTER TABLE "user"."user_stats"
    ADD CONSTRAINT "user_stats-stat_id" FOREIGN KEY ("stat_id") REFERENCES "public"."stats" ("id");
ALTER TABLE "user"."users"
    ADD CONSTRAINT "users-primary_rank" FOREIGN KEY ("primary_rank") REFERENCES "rank"."ranks" ("id");

INSERT INTO "rank"."ranks"(rank_name)
VALUES ('Default');