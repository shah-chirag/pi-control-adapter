#3.5.4.1-UAT
UPDATE users SET auth_type = NULL WHERE auth_type = 'NON_AD';
UPDATE users SET auth_type = 'NON_AD' WHERE auth_type IS NULL;
commit;