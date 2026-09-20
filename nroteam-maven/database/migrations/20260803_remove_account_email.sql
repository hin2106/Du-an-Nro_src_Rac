-- Email is no longer part of account registration or account identity.
-- Dropping the column also removes any index defined only on this column.

ALTER TABLE account
    DROP COLUMN email;
