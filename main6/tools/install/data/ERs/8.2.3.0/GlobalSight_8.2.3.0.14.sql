# GBS-2608
CREATE TABLE IF NOT EXISTS tm_profile_mt_info (
      `ID` bigint(20) AUTO_INCREMENT PRIMARY KEY,
      `tm_profile_id` bigint(20) NOT NULL,
      `mt_engine` varchar(50) DEFAULT NULL,
      `mt_key` varchar(50) DEFAULT NULL,
      `mt_value` varchar(200) DEFAULT NULL,
      CONSTRAINT FK_TM_PROFILE_ID FOREIGN KEY (tm_profile_id) REFERENCES TM_PROFILE(ID)
) AUTO_INCREMENT = 1;
