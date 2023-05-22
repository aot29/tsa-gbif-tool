alter table system add column `used_in_main`BOOL;
alter table system add column `GBIF_check` enum('ACCEPTED','SYNONYM','CONFLICTING','UNKNOWN','REVIEWED', 'IGNORE');
alter table system add column `GBIF_usage_key` int unsigned;
alter table system add column `GBIF_response` JSON;
update system set GBIF_check='IGNORE' where Artname="div.";
