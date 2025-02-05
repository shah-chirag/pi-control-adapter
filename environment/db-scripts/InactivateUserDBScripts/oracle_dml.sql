UPDATE ft42dbadmin.users SET user_state=cast('A' as  VARCHAR2(1)) WHERE id IN (SELECT user_id FROM ft42dbadmin.user_role_rel WHERE role_id IN (SELECT id FROM ft42dbadmin.roles WHERE name NOT IN ('USER','CONSUMER')));

COMMIT;

UPDATE ft42dbadmin.users SET user_state=cast('D' as  VARCHAR2(1)) WHERE id IN (SELECT user_id FROM ft42dbadmin.user_role_rel WHERE role_id IN (SELECT id FROM ft42dbadmin.roles WHERE name IN ('USER','CONSUMER')));

COMMIT;
insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.client.id', '0e50fbe72ae115ae5812');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.client.secret', '693916a12ad010cb79b064b0f35d76405cb3df588a8280ae49ac2eec7f2c');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 2, 'DF', 'fallout.process.vt.token.url', 'https://98.70.8.129:9443/identity-store/v6/fallout/vt-token');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.bank.id', 'ICI');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.username', 'ICI.VTUSER');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.password', 'p@king1');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.language.id', '001');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.channel.id', 'I');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.grant.type', 'password');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.api.key', 'mcl6Aov7fz41veXi5zHaehuouFfTjVFfjtdNGAShmsjVRTMy');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.deh.status', 'SUCCESS');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.ft42.status', 'FAILED');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.no.record.header.name', 'channelcontext');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.fips.enabled', 'false');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.no.mobile.number.message', 'mobile number doesnot exist in DEH');

insert into configs ( date_time_created, date_time_modified, version, type, key, value) values ( '5-Aug-2024, 1:00:35 pm', '5-Aug-2024, 1:00:35 pm', 1, 'DF', 'fallout.process.deh.url', 'https://98.70.8.129:9443/identity-store/v6/fallout/fallout-data');
commit ;

