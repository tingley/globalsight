# GBS-3697 McAfee SaaS: QA Checker.

CREATE TABLE `qa_filter` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(4000) DEFAULT NULL,
  `COMPANY_ID` bigint(20) unsigned NOT NULL,
  `CONFIG_XML` text,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table `company` add column ENABLE_QA_CHECKS char(1) DEFAULT 'N' AFTER ENABLE_TB_ACCESS_CONTROL;

alter table `project` add column ALLOW_MANUAL_QA_CHECKS char(1) DEFAULT 'N' AFTER SAVE_OFFLINE_FILES;
alter table `project` add column AUTO_ACCEPT_QA_TASK char(1) DEFAULT 'N' AFTER ALLOW_MANUAL_QA_CHECKS;
alter table `project` add column AUTO_SEND_QA_REPORT char(1) DEFAULT 'N' AFTER AUTO_ACCEPT_QA_TASK;

alter table `activity` add column QA_CHECKS char(1) DEFAULT 'N' AFTER IS_EDITABLE;

alter table `file_profile` add column QA_FILTER_ID BIGINT AFTER FILTER_TABLE_NAME;