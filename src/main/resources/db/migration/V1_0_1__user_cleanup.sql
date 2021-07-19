ALTER TABLE "user"."user_achievements"
    DROP CONSTRAINT "user_achievements_pkey",
    DROP COLUMN "id";

ALTER TABLE "user"."user_permissions"
    DROP CONSTRAINT "user_permissions_pkey",
    DROP COLUMN "id";

ALTER TABLE "user"."user_ranks"
    DROP CONSTRAINT "user_ranks_pkey",
    DROP COLUMN "id";

ALTER TABLE "user"."user_settings"
    DROP CONSTRAINT "user_settings_pkey",
    DROP COLUMN "id";

ALTER TABLE "user"."user_stats"
    DROP CONSTRAINT "user_stats_pkey",
    DROP COLUMN "id";