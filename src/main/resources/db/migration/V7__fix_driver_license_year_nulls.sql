-- Fill NULL license_year so NOT NULL constraint can apply (e.g. after partial run)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'drivers' AND column_name = 'license_year'
  ) THEN
    UPDATE drivers SET license_year = EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER - 5 WHERE license_year IS NULL;
    ALTER TABLE drivers ALTER COLUMN license_year SET NOT NULL;
  END IF;
END $$;
