# GBS-3722 McAfee SaaS: Ability to Tag Machine Translated Content

alter table `mt_profile` add column INCLUDE_MT_IDENTIFIERS char(1) DEFAULT 'N' AFTER SHOW_IN_EDITOR;
alter table `mt_profile` add column MT_IDENTIFIER_LEADING varchar(20) DEFAULT NULL AFTER INCLUDE_MT_IDENTIFIERS;
alter table `mt_profile` add column MT_IDENTIFIER_TRAILING varchar(20) DEFAULT NULL AFTER MT_IDENTIFIER_LEADING;