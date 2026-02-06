CREATE TABLE driver_license_categories (
    driver_id BIGINT NOT NULL,
    category VARCHAR(2) NOT NULL,
    PRIMARY KEY (driver_id, category),
    CONSTRAINT fk_driver_license_categories_driver
        FOREIGN KEY (driver_id) REFERENCES drivers (id) ON DELETE CASCADE
);

INSERT INTO driver_license_categories (driver_id, category)
SELECT id, license_category FROM drivers WHERE license_category IS NOT NULL AND license_category != '';

ALTER TABLE drivers DROP COLUMN license_category;
