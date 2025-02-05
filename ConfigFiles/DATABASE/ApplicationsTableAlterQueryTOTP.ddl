ALTER TABLE applications 
	ADD algorithm VARCHAR(20),
	ADD number_of_digits INT,
	ADD totp_expiry BIGINT;