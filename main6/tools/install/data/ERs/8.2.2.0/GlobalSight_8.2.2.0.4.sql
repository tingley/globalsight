#For GBS-2406: add "Content post-filter" to MS Office 2010 Filter.
ALTER TABLE `office2010_filter` CHANGE COLUMN `SECOND_FILTER_ID` `CONTENT_POST_FILTER_ID` bigint(20) NOT NULL;
ALTER TABLE `office2010_filter` CHANGE COLUMN `SECOND_FILTER_TABLE_NAME` `CONTENT_POST_FILTER_TABLE_NAME` varchar(45) NOT NULL;
