ALTER TABLE drivers
    ADD COLUMN license_year INTEGER;

UPDATE drivers
SET license_year = EXTRACT(YEAR FROM CURRENT_DATE) - experience;

ALTER TABLE drivers
    ALTER COLUMN license_year SET NOT NULL;

ALTER TABLE drivers
    DROP COLUMN experience;
