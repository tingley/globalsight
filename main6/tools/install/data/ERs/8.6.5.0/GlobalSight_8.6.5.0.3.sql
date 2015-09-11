# GBS-4039 : update default SRX rule

ALTER TABLE segmentation_rule
    ADD COLUMN IS_DEFAULT CHAR(1)
    NOT NULL DEFAULT 'N'
    CHECK(IS_DEFAULT IN ('Y', 'N'))
    AFTER IS_ACTIVE;

DELIMITER $$
DROP PROCEDURE IF EXISTS upgradeForGBS4039$$

CREATE PROCEDURE upgradeForGBS4039()
BEGIN
	DECLARE done INTEGER DEFAULT 0;

	DECLARE companyId BIGINT(20) DEFAULT 0;
	DECLARE srxId BIGINT(20) DEFAULT 0;
	DECLARE srxCount INTEGER DEFAULT 0;

	-- cursor
	DECLARE company_id_cur CURSOR FOR 
		SELECT id FROM company WHERE IS_ACTIVE = 'Y' AND id != 1;

	-- error handler 
	DECLARE EXIT HANDLER FOR SQLSTATE '02000' SET done = 1;
	DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' BEGIN END;

	OPEN company_id_cur;
	companyId_lable: LOOP
		FETCH company_id_cur INTO companyId;
		-- logger
		SELECT companyId AS CURRENT_COMPANY_ID;

		IF done = 1 THEN
			LEAVE companyId_lable;
		END IF;

		-- insert default rule if needed
		SELECT COUNT(*) INTO srxCount FROM `segmentation_rule` WHERE IS_DEFAULT = 'Y' AND COMPANY_ID = companyId;
		SELECT srxCount AS CURRENT_SRXCOUNT;
		IF srxCount = 0 THEN
			INSERT INTO `segmentation_rule`
			(`NAME`,
			`COMPANY_ID`,
			`SR_TYPE`,
			`DESCRIPTION`,
			`RULE_TEXT`,
			`IS_ACTIVE`,
			`IS_DEFAULT`)
			VALUES
			('GlobalSight Predefined',
			companyId,
			0,
			'Predefined Segmentation rule for GlobalSight.',
			'<?xml version="1.0"?>
<srx version="2.0" 
	xmlns="http://www.lisa.org/srx20"
	xsi:schemaLocation="http://www.lisa.org/srx20 srx20.xsd"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <header segmentsubflows="yes" cascade="yes">
        <formathandle type="start" include="no"/>
        <formathandle type="end" include="yes"/>
        <formathandle type="isolated" include="yes"/>
    </header>
    <body>
        <languagerules>
            <languagerule languagerulename="Default">
                <!-- Common rules for most languages -->
                <rule break="no">
                    <beforebreak>^\s*[0-9]+\.</beforebreak>
                    <afterbreak>\s</afterbreak>
                </rule>
                <rule break="yes">
                    <afterbreak>\n</afterbreak>
                </rule>
                <rule break="yes">
                    <beforebreak>[\.\?!]+</beforebreak>
                    <afterbreak>\s</afterbreak>
                </rule>
            </languagerule>
            <languagerule languagerulename="English">
                <!-- Some English abbreviations -->
                <rule break="no">
                    <beforebreak>\s[Ee][Tt][Cc]\.</beforebreak>
                    <afterbreak>\s[a-z]</afterbreak>
                </rule>
                <rule break="no">
                    <beforebreak>\sMr\.</beforebreak>
                    <afterbreak>\s</afterbreak>
                </rule>
                <rule break="no">
                    <beforebreak>\sU\.K\.</beforebreak>
                    <afterbreak>\s</afterbreak>
                </rule>
            </languagerule>
            <languagerule languagerulename="French">
                <!-- Some French abbreviations -->
                <rule break="no">
                    <beforebreak>\s[Mm]lle\.</beforebreak>
                    <afterbreak>\s</afterbreak>
                </rule>
                <rule break="no">
                    <beforebreak>\s[Mm]lles\.</beforebreak>
                    <afterbreak>\s</afterbreak>
                </rule>
                <rule break="no">
                    <beforebreak>\s[Mm]me\.</beforebreak>
                    <afterbreak>\s</afterbreak>
                </rule>
                <rule break="no">
                    <beforebreak>\s[Mm]mes\.</beforebreak>
                    <afterbreak>\s</afterbreak>
                </rule>
            </languagerule>
            <languagerule languagerulename="Japanese">
                <!-- Rules for breaking on Japanese punctuation
                
                \xff61: Halfwidth ideographic full stop
                \x3002: Ideographic full stop
                \xff0e: Fullwidth full stop
                \xff1f: Fullwidth question mark
                \xff01: Fullwidth exclamation mark
                -->
                <rule break="yes">
                    <beforebreak>[\xff61\x3002\xff0e\xff1f\xff01]+</beforebreak>
                    <afterbreak></afterbreak>
                </rule>
            </languagerule>
        </languagerules>
        <maprules>
            <!-- List exceptions first -->
            <languagemap languagepattern="[Ee][Nn].*" languagerulename="English"/>
            <languagemap languagepattern="[Ff][Rr].*" languagerulename="French"/>
            <!-- Japanese breaking rules -->
            <languagemap languagepattern="[Jj][Aa].*" languagerulename="Japanese"/>
            <!-- Common breaking rules -->
            <languagemap languagepattern=".*" languagerulename="Default"/>
        </maprules>
    </body>
</srx>',
			'Y', 'Y'
			);

			select @@identity into srxId;
		ELSE
			SELECT ID INTO srxId FROM `segmentation_rule` WHERE IS_DEFAULT = 'Y' AND COMPANY_ID = companyId;
		END IF;
		
		-- logger
		SELECT srxId AS CURRENT_SRXID;
		
		-- update tm profile - segmentation rule
		INSERT INTO `segmentation_rule_tm_profile`
		(`SEGMENTATION_RULE_ID`,
		`TM_PROFILE_ID`)
		SELECT srxId, ID FROM `tm_profile` TMP left join `segmentation_rule_tm_profile` SRXTMP on TMP.ID = SRXTMP.TM_PROFILE_ID 
		WHERE SRXTMP.TM_PROFILE_ID IS NULL and TMP.COMPANY_ID = companyId;


	END LOOP;
	CLOSE company_id_cur;
    END$$

DELIMITER ;


CALL upgradeForGBS4039;
DROP PROCEDURE IF EXISTS upgradeForGBS4039;