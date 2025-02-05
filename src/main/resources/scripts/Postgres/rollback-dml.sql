#3.5.4.1-UAT
UPDATE users SET auth_type = NULL WHERE auth_type = 'NON_AD';
commit;