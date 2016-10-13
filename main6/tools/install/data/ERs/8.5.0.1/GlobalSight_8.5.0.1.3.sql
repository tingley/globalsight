-- [GBS-3135] Create Reports Data Table.
CREATE TABLE REPORTS_DATA (                                                                                          
	USER_ID varchar(100) NOT NULL,
	REPORT_JOBIDS varchar(500) NOT NULL DEFAULT '',
	REPORT_TYPELIST varchar(500) NOT NULL DEFAULT '',
	STATUS varchar(100) DEFAULT NULL,
	PERCENT double DEFAULT NULL,
	PRIMARY KEY (USER_ID,REPORT_JOBIDS(255),REPORT_TYPELIST(255)) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;      