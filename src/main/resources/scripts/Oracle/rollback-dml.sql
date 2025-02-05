#3.5.4.1-UAT
UPDATE USERS SET AUTH_TYPE = NULL WHERE AUTH_TYPE = 'NON_AD';
COMMIT;

#3.6.3 - start
UPDATE fallout_configs SET scheduler_frequency=24;
#3.6.3 - stop

#3.6.4 - start

DELETE FROM configs WHERE key = 'fallout.process.client.id';
DELETE FROM configs WHERE key = 'fallout.process.client.secret';
DELETE FROM configs WHERE key = 'fallout.process.vt.token.url';
DELETE FROM configs WHERE key = 'fallout.process.bank.id';
DELETE FROM configs WHERE key = 'fallout.process.username';
DELETE FROM configs WHERE key = 'fallout.process.password';
DELETE FROM configs WHERE key = 'fallout.process.language.id';
DELETE FROM configs WHERE key = 'fallout.process.channel.id';
DELETE FROM configs WHERE key = 'fallout.process.grant.type';
DELETE FROM configs WHERE key = 'fallout.process.api.key';
DELETE FROM configs WHERE key = 'fallout.process.deh.status';
DELETE FROM configs WHERE key = 'fallout.process.ft42.status';
DELETE FROM configs WHERE key = 'fallout.process.no.record.header.name';
DELETE FROM configs WHERE key = 'fallout.process.fips.enabled';
DELETE FROM configs WHERE key = 'fallout.process.no.mobile.number.message';
DELETE FROM configs WHERE key = 'fallout.process.deh.url';
#3.6.4 - end

#template details
delete from validation_rule where template_id=(select id from template_details where template_details.template_id='DEFAULT' and template_details.type ='SMS' );
delete from template_details where template_details.template_id='DEFAULT' and template_details.type ='SMS';
delete from validation_rule where template_id=(select id from template_details where template_details.template_id='DEFAULT' and template_details.type ='EMAIL' );
delete from template_details where template_details.template_id='DEFAULT' and template_details.type ='EMAIL';
commit;