CREATE TABLE IF NOT EXISTS CATEGORY
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  CATEGORY VARCHAR(200),
  COMPANY_ID BIGINT NOT NULL,
  CONSTRAINT FK_CATEGORY_COMPANY FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID)
) AUTO_INCREMENT = 1;
