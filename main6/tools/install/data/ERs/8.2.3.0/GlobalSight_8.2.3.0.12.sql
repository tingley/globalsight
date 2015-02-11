# GBS-2588: on restart, dump out jms_messages table before deletion
CREATE TABLE jms_messages_debug (
	MESSAGEID int(11) NOT NULL,
	DESTINATION varchar(150) NOT NULL,
	TXID int(11) DEFAULT NULL,
        TXOP char(1) DEFAULT NULL,
	MESSAGEBLOB longblob,
	BACKUP_TIME DATETIME
) ENGINE=InnoDB;