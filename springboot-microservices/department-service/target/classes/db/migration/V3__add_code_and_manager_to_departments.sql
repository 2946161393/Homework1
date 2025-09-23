-- Add code column (required, unique)
ALTER TABLE department.departments
    ADD COLUMN IF NOT EXISTS code VARCHAR(20);

ALTER TABLE department.departments
    ADD COLUMN IF NOT EXISTS manager_email VARCHAR(200);

-- Update existing departments with default codes
UPDATE department.departments
SET code = UPPER(SUBSTRING(REPLACE(name, ' ', '-'), 1, 10)) || '-' || id::text
WHERE code IS NULL;

-- Make code not null and unique
ALTER TABLE department.departments
    ALTER COLUMN code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_departments_code
    ON department.departments(LOWER(code));