alter table applications add column application_account_id varchar(30);

alter table applications add column enterprise_account_id varchar(30);

create table attribute_store (id  bigserial not null, attribute_name varchar(255), attribute_relation_entity varchar(255), attribute_relation_id varchar(255), attribute_security_policy varchar(255), attribute_type int4, attribute_value varchar(255), auth_attempt_id int8, evidence_hash varchar(255), sign_transaction_id varchar(255), user_id int8, primary key (id));

alter table authentication_attempts add column approval_attempt_mode varchar(40);

alter table authentication_attempts add column enterprise_id varchar(20);

alter table authentication_attempts add column user_identifier varchar(30);

create table evidence_store (id  bigserial not null, attribute_id int8, evidence text, file_path varchar(255), media_type varchar(255), primary key (id));

alter table staging_users add column account_id varchar(20);

alter table users add column account_id varchar(30);

-- IAM-1251-start --
alter table authentication_attempts alter column attempt_status type character varying(25);

insert into permissions(date_time_created,date_time_modified,name,version,active) values('2020-07-06 15:15:32.564442','2020-07-06 15:15:32.564442','APPLICATION_ONBOARD',0,'T');

insert into permissions(date_time_created,date_time_modified,name,version,active) values('2020-07-06 15:15:32.564442','2020-07-06 15:15:32.564442','APPROVE_APPLICATION_ONBOARD',0,'T');

INSERT INTO role_permission(role_id, permission_id) select roles.id as role_id, permissions.id as permission_id from roles, permissions where roles.name = 'MAKER' and permissions.name = 'APPLICATION_ONBOARD';

INSERT INTO role_permission(role_id, permission_id) select roles.id as role_id, permissions.id as permission_id from roles, permissions where roles.name = 'CHECKER' and permissions.name = 'APPROVE_APPLICATION_ONBOARD';

-- IAM-1251-End --

-- IAM-1352-Start --
alter table ft42dbadmin.applications alter column application_id type character varying(25);

ALTER TABLE ft42dbadmin.applications MODIFY (application_id VARCHAR2(25));

-- IAM-1352-End --

-- IAM-1467-Start --
ALTER TABLE ft42dbadmin.role_permission
RENAME TO role_permission_rel;

INSERT INTO ft42dbadmin.enterprises (
date_time_created, id, date_time_modified, version, enterprise_account_id, enterprise_id, enterprise_name, status) VALUES (
'2020-07-06 15:16:16.784451'::timestamp without time zone, '1'::bigint, '2020-07-06 15:16:16.784451'::timestamp without time zone, '1'::integer, '607998c5ab5bfc222f930b74'::character varying, 'FT42NEW'::character varying, 'New Enterprise'::character varying, 'ACTIVE'::character varying)
 returning id;
 INSERT INTO ft42dbadmin.enterprises (
date_time_created, id, date_time_modified, version, enterprise_account_id, enterprise_id, enterprise_name, status) VALUES (
'2020-07-06 15:16:16.784451'::timestamp without time zone, '2'::bigint, '2020-07-06 15:16:16.784451'::timestamp without time zone, '1'::integer, '607998c5ab5bfc222f930b74'::character varying, 'MyMoneyBk'::character varying, 'New Enterprise'::character varying, 'ACTIVE'::character varying)
 returning id;
 
 INSERT INTO ft42dbadmin.enterprises (
date_time_created, id, date_time_modified, version, enterprise_account_id, enterprise_id, enterprise_name, status) VALUES (
'2020-07-06 15:16:16.784451'::timestamp without time zone, '3'::bigint, '2020-07-06 15:16:16.784451'::timestamp without time zone, '1'::integer, '607998c5ab5bfc222f930b74'::character varying, 'FT42'::character varying, 'New Enterprise'::character varying, 'ACTIVE'::character varying)
 returning id;
 
 INSERT INTO ft42dbadmin.enterprises (
date_time_created, id, date_time_modified, version, enterprise_account_id, enterprise_id, enterprise_name, status) VALUES (
'2020-07-06 15:16:16.784451'::timestamp without time zone, '4'::bigint, '2020-07-06 15:16:16.784451'::timestamp without time zone, '1'::integer, '5fd07adabf23ac01c80d1c8f'::character varying, 'FORTYTWO42'::character varying, 'New Enterprise'::character varying, 'ACTIVE'::character varying)
 returning id;
 
 INSERT INTO ft42dbadmin.enterprises (
date_time_created, id, date_time_modified, version, enterprise_account_id, enterprise_id, enterprise_name, status) VALUES (
'2020-07-06 15:16:16.784451'::timestamp without time zone, '5'::bigint, '2020-07-06 15:16:16.784451'::timestamp without time zone, '1'::integer, '60f55620fe1a4233c220f73b'::character varying, 'FORTYTWO42NEW'::character varying, 'New Enterprise'::character varying, 'ACTIVE'::character varying)
 returning id;
 
  INSERT INTO ft42dbadmin.enterprises (
date_time_created, id, date_time_modified, version, enterprise_account_id, enterprise_id, enterprise_name, status) VALUES (
'2020-07-06 15:16:16.784451'::timestamp without time zone, '6'::bigint, '2020-07-06 15:16:16.784451'::timestamp without time zone, '1'::integer, '614190a8702f60541ce3222e'::character varying, 'FT42020'::character varying, 'Income Tax India 3'::character varying, 'ACTIVE'::character varying)
 returning id;
 
 INSERT INTO ft42dbadmin.roles (
active, date_time_created, date_time_modified, name, version, id) VALUES (
'T', '2020-07-06 15:16:16.797794'::timestamp without time zone, '2020-07-06 15:16:16.797794'::timestamp without time zone, 'USER'::character varying, '0'::integer, '7'::bigint)
 returning id;
 
 INSERT INTO ft42dbadmin.roles (
active, date_time_created, date_time_modified, name, version, id) VALUES (
'T', '2020-07-06 15:16:16.797794'::timestamp without time zone, '2020-07-06 15:16:16.797794'::timestamp without time zone, 'SUPER_USER'::character varying, '0'::integer, '6'::bigint)
 returning id;
 
 
 UPDATE ft42dbadmin.services SET
date_time_created = '2021-09-15 15:28:32.901'::timestamp without time zone, date_time_modified = '2021-09-15 15:28:32.901'::timestamp without time zone, version = '0'::integer WHERE
id = '1';
reboot adapter9 after the above update for the changes to reflect

 
 new enterprise setup
 


INSERT INTO ft42dbadmin.application_service_rel (application_id, service_id) select applications.id as application_id, services.id as service_id from ft42dbadmin.applications, ft42dbadmin.services where applications.application_id = 'FT420200001' and services.service_name = 'APPROVAL';

INSERT INTO ft42dbadmin.applications ( active, application_id, application_name, application_secret, application_type, authentication_required, date_time_created, date_time_modified, description, modified_by, password, transaction_timeout, twofactor_status, version, unblock_settings, application_account_id ) values ('T' , 'FT420200001',  'FT420200001' , 'aAVpdgarxl0jzeJGnVT11A==','NON_AD', 'T', current_timestamp, current_timestamp, 'CNB FED', 'SYSTEM', 'byO2NHIpUIcS3JGDL8mGrQ==', '90', 'ENABLED', '0',  'AUTO_UNBLOCK',  '61419912702f60541ce32234');

removing old users

DELETE FROM ft42dbadmin.users
    WHERE account_id is NULL;
    
adding permission for suer_user

INSERT INTO ft42dbadmin.role_permission_rel(
	role_id, permission_id)
	VALUES (6, 15); view_applications
INSERT INTO ft42dbadmin.role_permission_rel(
	role_id, permission_id) 
	VALUES (6, 14); view_non_ad_users
INSERT INTO ft42dbadmin.role_permission_rel(
	role_id, permission_id) 
	VALUES (6, 8); edit_non_ad_users	
	
-- IAM-1467-END --

-- IAM-2845-START--

CREATE TABLE ft42dbadmin.server_registries (
	id bigserial NOT NULL,
	serverId varchar(50) NULL,
	status varchar(50) NULL,
	ip varchar(50) NULL,
	constraint server_registries_pkey PRIMARY KEY (id)
);

-- IAM-2845-END--

-- 3.5.4.7 - fallout - start
create table fallout_sync_data(id  bigserial not null, date_time_created timestamp, date_time_modified timestamp, version int4,last_sync_time timestamp);
insert into fallout_sync_data(date_time_created,date_time_modified,version,last_sync_time) values(TO_TIMESTAMP('2024-02-03 19:30:00.000','YYYY-MM-DD HH24:MI:SS.FF'),TO_TIMESTAMP('2024-02-03 19:30:00.000','YYYY-MM-DD HH24:MI:SS.FF'),0,TO_TIMESTAMP('2024-02-03 19:30:00.000','YYYY-MM-DD HH24:MI:SS.FF'));
-- 3.5.4.7 - fallout - end