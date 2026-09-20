-- Run after checking and resolving duplicate data in the existing database.
-- These constraints prevent races during account registration and character creation.

ALTER TABLE account
    ADD UNIQUE KEY uq_account_username (username);

ALTER TABLE player
    ADD UNIQUE KEY uq_player_account_id (account_id),
    ADD UNIQUE KEY uq_player_name (name);
