# for gbs-1139
CREATE TABLE ADDING_SOURCE_PAGE
(
  ID BIGINT
    AUTO_INCREMENT  
    PRIMARY KEY,
  JOB_ID BIGINT NOT NULL,
  L10N_PROFILE_ID BIGINT NOT NULL,
  DATA_SOURCE VARCHAR(100) NOT NULL,
  EXTERNAL_PAGE_ID VARCHAR(4000) NOT NULL
) AUTO_INCREMENT = 1000;