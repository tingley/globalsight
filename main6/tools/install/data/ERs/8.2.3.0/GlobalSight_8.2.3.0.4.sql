# GBS-2461 & 2462: Auto accept task and auto send report function in Project Level.

ALTER TABLE project add column (
REVIEWONLYAUTOACCEPT tinyint(1) NOT NULL DEFAULT '0',
REVIEWONLYAUTOSEND tinyint(1) NOT NULL DEFAULT '0',
AUTOACCEPTPMTASK tinyint(1) NOT NULL DEFAULT '0'
);
