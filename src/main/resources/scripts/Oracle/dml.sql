#3.5.4.1-UAT
UPDATE USERS SET AUTH_TYPE = NULL WHERE AUTH_TYPE = 'NON_AD';
UPDATE USERS SET AUTH_TYPE = 'NON_AD' WHERE AUTH_TYPE IS NULL;
COMMIT;

#3.6.3 - start
UPDATE fallout_configs SET scheduler_frequency='0,1,2,3,4,5,6,7,21,22,23,24';
#3.6.3 - stop
#3.6.4
update attributes_store set attribute_value = upper(attributes_store.attribute_value);
commit;

#3.6.4 - start

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

#3.6.4 - end

#3.6.5 Mongo Query

db.getCollection("accounts").find({$or:[{'account_type':"OPERATIONAL_MAKER"},{'account_type':"OPERATIONAL_CHECKER"},{'account_type':"OPERATIONAL_VIEWONLY"},{'account_type':"APPLICATION_MAKER"},{'account_type':"APPLICATION_CHECKER"},{'account_type':"APPLICATION_VIEWONLY"},{'account_type':"USER_MAKER"},{'account_type':"USER_CHECKER"},{'account_type':"USER_VIEWONLY"}]}).forEach((x)=>{
    var account_type=x.account_type
    console.log(x._id+' '+x.account_type)
    db.getCollection("accounts").updateOne({'_id':x._id},[{$set: { 'account_type' : 'USER'}}])
})

#template details

INSERT INTO ft42dbadmin.template_details
(date_time_created, date_time_modified, version, template, template_id, type)
VALUES (
    TO_TIMESTAMP('2024-09-10 06:31:27.675', 'YYYY-MM-DD HH24:MI:SS.FF3'),
    TO_TIMESTAMP('2024-09-10 06:31:27.675', 'YYYY-MM-DD HH24:MI:SS.FF3'),
    1,
    'Dear Customer, <OTP> is the OTP to LOGIN to %s. OTPs are SECRET. DO NOT disclose it to anyone. %s NEVER asks for OTP.',
    'DEFAULT',
    'SMS');

    INSERT INTO ft42dbadmin.validation_rule
(date_time_created, date_time_modified, version, template_rule, template_id,validation_return)
VALUES (
    TO_TIMESTAMP('2024-09-10 06:31:27.675', 'YYYY-MM-DD HH24:MI:SS.FF3'),
    TO_TIMESTAMP('2024-09-10 06:31:27.675', 'YYYY-MM-DD HH24:MI:SS.FF3'),
    1,
    '((((f|ht)tps?:)?//)?([a-zA-Z0-9!#$%&''*+-/=?^_`{|}~]+(:[^ @:]+)?@)?((([a-zA-Z0-9\\-]{1,255}|xn--[a-zA-Z0-9\\-]+)\\.)+(xn--[a-zA-Z0-9\\-]+|[a-zA-Z]{2,6}|\\d{1,3})|localhost|(%[0-9a-fA-F]{2})+|[0-9]+)(:[0-9]{1,5})?([/\\?][^ \\s/]*)*)',
    1 ,
1
);

INSERT INTO ft42dbadmin.template_details
(date_time_created, date_time_modified, version, template, template_id, type)
VALUES (
    TO_TIMESTAMP('2024-09-10 06:31:27.675', 'YYYY-MM-DD HH24:MI:SS.FF3'),
    TO_TIMESTAMP('2024-09-10 06:31:27.675', 'YYYY-MM-DD HH24:MI:SS.FF3'),
    1,
    'Dear Customer, <OTP> is the OTP to LOGIN to %s. OTPs are SECRET. DO NOT disclose it to anyone. %s NEVER asks for OTP.',
    'DEFAULT',
    'EMAIL'
);

INSERT INTO ft42dbadmin.validation_rule
(date_time_created, date_time_modified, version, template_rule, template_id, validation_return)
VALUES (
    TO_TIMESTAMP('2024-09-10 06:31:27.675', 'YYYY-MM-DD HH24:MI:SS.FF3'),
    TO_TIMESTAMP('2024-09-10 06:31:27.675', 'YYYY-MM-DD HH24:MI:SS.FF3'),
    1,
    '((((f|ht)tps?:)?//)?([a-zA-Z0-9!#$%&''*+-/=?^_`{|}~]+(:[^ @:]+)?@)?((([a-zA-Z0-9\\-]{1,255}|xn--[a-zA-Z0-9\\-]+)\\.)+(xn--[a-zA-Z0-9\\-]+|[a-zA-Z]{2,6}|\\d{1,3})|localhost|(%[0-9a-fA-F]{2})+|[0-9]+)(:[0-9]{1,5})?([/\\?][^ \\s/]*)*)',
    2 ,
1
);
