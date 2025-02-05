#3.5.4.1-UAT
ALTER TABLE USERS DROP COLUMN AUTH_TYPE;
COMMIT;

ALTER TABLE users DROP (last_lockout_time, retries_left);


#3.6.3 - start
ALTER TABLE fallout_configs DROP COLUMN scheduler_frequency;
ALTER TABLE fallout_configs ADD scheduler_frequency NUMBER;
3.6.3 - stop

#OTP AUDIT LOG - start
DROP TABLE otp_audit_logs_validate;
DROP TABLE otp_audit_logs_send;
#OTP AUDIT LOG - end

#3.6.4

drop table ldap_details;
drop table admin_audit_logs;
alter table applications drop column is_fcm_multidevice;
commit ;

DROP TABLE totp_audit_trail;
drop table configs;
ALTER TABLE configs drop CONSTRAINT unique_config_key;
ALTER TABLE ldap_details drop CONSTRAINT domain_name_constraint ;

#3.6.4 - start
DROP INDEX idx_fallout_data_user_id;
DROP INDEX idx_fallout_data_mobile_no;
#3.6.4 - end

drop index totp_status_idx;
drop index  application_name_idx;
drop index identifier_value_idx;
drop index date_time_created_idx;
drop index role_idx;
drop index status_idx;
drop index username_idx;
drop index login_time_idx;
#template details
drop TABLE VALIDATION_RULE;
drop TABLE TEMPLATE_DETAILS;