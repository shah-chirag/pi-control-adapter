#3.5.4.1-UAT
ALTER TABLE users DROP COLUMN auth_type;
ALTER TABLE users ADD COLUMN auth_type VARCHAR(10);
commit;