#3.5.6.4
INSERT INTO ft42dbadmin.roles(date_time_created, date_time_modified, name, version) VALUES (TO_TIMESTAMP('2024-01-15 15:16:16.784451','YYYY-MM-DD HH24:MI:SS.FF'),TO_TIMESTAMP('2024-01-15 15:16:16.784451','YYYY-MM-DD HH24:MI:SS.FF'),'OPERATIONAL_MAKER',0);
INSERT INTO ft42dbadmin.roles(date_time_created, date_time_modified, name, version) VALUES (TO_TIMESTAMP('2024-01-15 15:16:16.784451','YYYY-MM-DD HH24:MI:SS.FF'),TO_TIMESTAMP('2024-01-15 15:16:16.784451','YYYY-MM-DD HH24:MI:SS.FF'),'OPERATIONAL_CHECKER',0);
INSERT INTO ft42dbadmin.roles(date_time_created, date_time_modified, name, version) VALUES (TO_TIMESTAMP('2024-01-15 15:16:16.784451','YYYY-MM-DD HH24:MI:SS.FF'),TO_TIMESTAMP('2024-01-15 15:16:16.784451','YYYY-MM-DD HH24:MI:SS.FF'),'OPERATIONAL_VIEWONLY',0);


insert into ft42dbadmin.role_permission_rel(role_id,permission_id) values ((select id from roles where name='OPERATIONAL_MAKER'),(select id from permissions where name='ATTRIBUTE_MASTER_ADDITION'));
insert into ft42dbadmin.role_permission_rel(role_id,permission_id) values ((select id from roles where name='OPERATIONAL_MAKER'),(select id from permissions where name='ATTRIBUTE_MASTER_DELETION'));
insert into ft42dbadmin.role_permission_rel(role_id,permission_id) values ((select id from roles where name='OPERATIONAL_MAKER'),(select id from permissions where name='ATTRIBUTE_MASTER_UPDATION'));


commit;







