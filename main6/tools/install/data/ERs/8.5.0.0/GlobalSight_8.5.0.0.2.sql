DELIMITER $$

DROP PROCEDURE IF EXISTS PROC_UPDATE_FOR_MT_PROFILE$$

# For GBS-3016: Use one TM profile and Loc profile for all locales whether all or some languages require MT.
# In GBS-3016 implementation, MT options are moved to MT profiles separatly away from TM profile.
CREATE PROCEDURE PROC_UPDATE_FOR_MT_PROFILE()

    BEGIN
	DECLARE done INTEGER DEFAULT 0;

	DECLARE v_tmProfileID INTEGER;
	DECLARE v_tmProfileName VARCHAR(60);
	DECLARE v_companyID INTEGER;

	-- declare v_userMT char(1);
	DECLARE v_mtEngine VARCHAR(60);
	DECLARE v_mtConfidenceScore INTEGER;
	DECLARE v_showInEditor CHAR(1);

	DECLARE v_ptsUrl VARCHAR(100);
	DECLARE v_ptsUserName VARCHAR(100);
	DECLARE v_ptsPassword VARCHAR(100);
	-- declare v_urlFlag char(1);

	DECLARE v_msMtUrl VARCHAR(100);
	DECLARE v_msMtCategory VARCHAR(50);
	DECLARE v_msMtClientID VARCHAR(100);
	DECLARE v_msMtClientSecret VARCHAR(100);
	-- declare v_msMtAppID varchar(50);
	-- declare v_msMtUrlFlag char(1);

	DECLARE v_aoUrl VARCHAR(100);
	DECLARE v_aoPort INTEGER;
	DECLARE v_aoUserName VARCHAR(100);
	DECLARE v_aoPassword VARCHAR(100);
	DECLARE v_aoAccountNumber INTEGER;
	
	DECLARE v_safaMtHost VARCHAR(200);
	DECLARE v_safaMtPort VARCHAR(10);
	DECLARE v_safaMtCompanyName VARCHAR(40);
	DECLARE v_safaMtPassword VARCHAR(40);
	DECLARE v_safaClient VARCHAR(40);
	
	DECLARE v_maxMtProfileID INTEGER;

	-- cursor
	DECLARE tm_profile_cur CURSOR FOR
		SELECT tmp.ID, tmp.NAME, tm.COMPANY_ID, tmp.MT_ENGINE, tmp.MT_CONFIDENCE_SCORE, tmp.MT_SHOW_IN_EDITOR
		FROM tm_profile tmp, project_tm tm
		WHERE tmp.PROJECT_TM_ID_FOR_SAVE = tm.ID
		AND tmp.USE_MT = 'Y'
		AND tmp.MT_ENGINE IS NOT NULL;

	-- error handler 
	DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;

	OPEN tm_profile_cur;
	    tm_profile_lable: LOOP

		FETCH tm_profile_cur INTO v_tmProfileID, v_tmProfileName, v_companyID, v_mtEngine, v_mtConfidenceScore, v_showInEditor;
		-- logger
		SELECT done AS done_value;
		SELECT CONCAT(v_tmProfileID, '--', v_tmProfileName, '--', v_companyID, '--', v_mtEngine, '--', v_mtConfidenceScore, '--', v_showInEditor) AS tm_profile_cur_info;

		IF done = 1 THEN
			SELECT 'Upgrading is done, end loop.' AS MESSAGE;
			LEAVE tm_profile_lable;
		END IF;

		SET @mtProfileName = CONCAT('mtProfile_', v_mtEngine, '_', v_tmProfileName);
		SET @description = CONCAT('Upgraded from TM profile: ', v_tmProfileName);

		### ProMT
		IF v_mtEngine = 'ProMT' THEN
			SELECT PTSURL, PTS_USERNAME, PTS_PASSWORD INTO v_ptsUrl, v_ptsUserName, v_ptsPassword FROM tm_profile WHERE ID = v_tmProfileID;
			-- logger
			SELECT CONCAT(v_ptsUrl, '--', v_ptsUserName, '--', v_ptsPassword) AS pts_info;
			
			# "mt_profile" table
			INSERT INTO mt_profile (
				MT_PROFILE_NAME, MT_ENGINE, DESCRIPTION, MT_CONFIDENCE_SCORE, URL, 
				PORT, USERNAME, PASSWORD, CATEGORY, ACCOUNTINFO, 
				COMPANY_ID, TIMESTAMP, SHOW_IN_EDITOR, IS_ACTIVE)
			VALUES (@mtProfileName, v_mtEngine, @description, v_mtConfidenceScore, v_ptsUrl, 
				NULL, v_ptsUserName, v_ptsPassword, NULL, NULL, 
				v_companyID, NOW(), v_showInEditor, 'Y');

			# "mt_profile_extent_info" table
			SELECT MAX(id) INTO v_maxMtProfileID FROM mt_profile;
			INSERT INTO mt_profile_extent_info (MT_PROFILE_ID, LANGUAGE_PAIR_CODE, LANGUAGE_PAIR_NAME, DOMAIN_CODE)
			SELECT v_maxMtProfileID, DIR_ID, DIR_NAME, TOPIC_TEMPLATE_ID FROM tm_profile_promt_info
			WHERE TM_PROFILE_ID = v_tmProfileID;

		### MS Translator
		ELSEIF v_mtEngine = 'MS_Translator' THEN
			SELECT MS_MT_URL, MS_MT_CATEGORY, MS_MT_CLIENTID, MS_MT_CLIENT_SECRET 
			INTO v_msMtUrl, v_msMtCategory, v_msMtClientID, v_msMtClientSecret
			FROM tm_profile
			WHERE ID = v_tmProfileID;
			-- logger
			SELECT CONCAT(v_msMtUrl, '--', v_msMtCategory, '--', v_msMtClientID, '--', v_msMtClientSecret) AS ms_translator_info;

			# "mt_profile" table
			INSERT INTO mt_profile (
				MT_PROFILE_NAME, MT_ENGINE, DESCRIPTION, MT_CONFIDENCE_SCORE, URL,
				PORT, USERNAME, PASSWORD, CATEGORY, ACCOUNTINFO,
				COMPANY_ID, TIMESTAMP, SHOW_IN_EDITOR, IS_ACTIVE)
			VALUES (@mtProfileName, v_mtEngine, @description, v_mtConfidenceScore, v_msMtUrl,
				NULL, v_msMtClientID, v_msMtClientSecret, v_msMtCategory, NULL,
				v_companyID, NOW(), v_showInEditor, 'Y');

		### Asia_Online
		ELSEIF v_mtEngine = 'Asia_Online' THEN
			SELECT AO_URL, AO_PORT, AO_USERNAME, AO_PASSWORD, AO_ACCOUNT_NUMBER 
			INTO v_aoUrl, v_aoPort, v_aoUserName, v_aoPassword, v_aoAccountNumber 
			FROM tm_profile 
			WHERE ID = v_tmProfileID;
			-- logger
			SELECT CONCAT(v_aoUrl, '--', v_aoPort, '--', v_aoUserName, '--', v_aoPassword, '--', v_aoAccountNumber) AS asia_online_info;

			# "mt_profile" table
			INSERT INTO mt_profile (
				MT_PROFILE_NAME, MT_ENGINE, DESCRIPTION, MT_CONFIDENCE_SCORE, URL,
				PORT, USERNAME, PASSWORD, CATEGORY, ACCOUNTINFO,
				COMPANY_ID, TIMESTAMP, SHOW_IN_EDITOR, IS_ACTIVE)
			VALUES (
				@mtProfileName, v_mtEngine, @description, v_mtConfidenceScore, v_aoUrl,
				v_aoPort, v_aoUserName, v_aoPassword, NULL, v_aoAccountNumber,
				v_companyID, NOW(), v_showInEditor, 'Y');

			# "mt_profile_extent_info" table
			SELECT MAX(id) INTO v_maxMtProfileID FROM mt_profile;
			INSERT INTO mt_profile_extent_info (MT_PROFILE_ID, LANGUAGE_PAIR_CODE, LANGUAGE_PAIR_NAME, DOMAIN_CODE)
			SELECT v_maxMtProfileID, LANGUAGE_PAIR_CODE, LANGUAGE_PAIR_NAME, DOMAIN_COMBINATION_CODE 
			FROM tm_profile_ao_info
			WHERE TM_PROFILE_ID = v_tmProfileID;

		### Safaba
		ELSEIF v_mtengine = 'Safaba' THEN
			SELECT mt_value INTO v_safaMtHost FROM tm_profile_mt_info WHERE tm_profile_id = v_tmProfileID AND mt_engine = 'safaba' AND mt_key = 'safa_mt_host';
			SELECT mt_value INTO v_safaMtPort FROM tm_profile_mt_info WHERE tm_profile_id = v_tmProfileID AND mt_engine = 'safaba' AND mt_key = 'safa_mt_port';
			SELECT mt_value INTO v_safaMtCompanyName FROM tm_profile_mt_info WHERE tm_profile_id = v_tmProfileID AND mt_engine = 'safaba' AND mt_key = 'safa_mt_company_name';
			SELECT mt_value INTO v_safaMtPassword FROM tm_profile_mt_info WHERE tm_profile_id = v_tmProfileID AND mt_engine = 'safaba' AND mt_key = 'safa_mt_password';
			SELECT mt_value INTO v_safaClient FROM tm_profile_mt_info WHERE tm_profile_id = v_tmProfileID AND mt_engine = 'safaba' AND mt_key = 'safaba_client';
			-- logger			
			SELECT CONCAT(v_safaMtHost, '--', v_safaMtPort, '--', v_safaMtCompanyName, '--', v_safaMtPassword, '--', v_safaClient) AS safaba_info;

			# "mt_profile" table
			INSERT INTO mt_profile (
				MT_PROFILE_NAME, MT_ENGINE, DESCRIPTION, MT_CONFIDENCE_SCORE, URL,
				PORT, USERNAME, PASSWORD, CATEGORY, ACCOUNTINFO,
				COMPANY_ID, TIMESTAMP, SHOW_IN_EDITOR, IS_ACTIVE)
			VALUES (
				@mtProfileName, v_mtEngine, @description, v_mtConfidenceScore, v_safaMtHost,
				v_safaMtPort, v_safaMtCompanyName, v_safaMtPassword, NULL, v_safaClient,
				v_companyID, NOW(), v_showInEditor, 'Y');
		END IF;

		### Update 'l10n_profile_wftemplate_info' table 'mt_profile_id" column.
		SELECT MAX(id) INTO v_maxMtProfileID FROM mt_profile;
		UPDATE l10n_profile_wftemplate_info lpwi, l10n_profile_tm_profile lptp 
		SET lpwi.mt_profile_id = v_maxMtProfileID
		WHERE lpwi.l10n_profile_id = lptp.l10n_profile_id
		AND lptp.tm_profile_id = v_tmProfileID;

		COMMIT;

	    END LOOP;

	CLOSE tm_profile_cur;

	### drop related old tables and columns.
	-- DROP TABLE IF EXISTS tm_profile_ao_info;
	-- DROP TABLE IF EXISTS tm_profile_mt_info;
	-- DROP TABLE IF EXISTS tm_profile_promt_info;

	-- ALTER TABLE tm_profile DROP COLUMN MT_ENGINE;
	-- ALTER TABLE tm_profile DROP COLUMN USE_MT;
	-- ALTER TABLE tm_profile DROP COLUMN MT_CONFIDENCE_SCORE;
	-- ALTER TABLE tm_profile DROP COLUMN MT_SHOW_IN_EDITOR;
	-- ALTER TABLE tm_profile DROP COLUMN PTSURL;
	-- ALTER TABLE tm_profile DROP COLUMN PTS_USERNAME;
	-- ALTER TABLE tm_profile DROP COLUMN PTS_PASSWORD;
	-- ALTER TABLE tm_profile DROP COLUMN PTS_URL_FLAG;
	-- ALTER TABLE tm_profile DROP COLUMN MS_MT_URL;
	-- ALTER TABLE tm_profile DROP COLUMN MS_MT_APPID;
	-- ALTER TABLE tm_profile DROP COLUMN MS_MT_CATEGORY;
	-- ALTER TABLE tm_profile DROP COLUMN MS_MT_CLIENTID;
	-- ALTER TABLE tm_profile DROP COLUMN MS_MT_CLIENT_SECRET;
	-- ALTER TABLE tm_profile DROP COLUMN MS_MT_URL_FLAG;
	-- ALTER TABLE tm_profile DROP COLUMN AO_URL;
	-- ALTER TABLE tm_profile DROP COLUMN AO_PORT;
	-- ALTER TABLE tm_profile DROP COLUMN AO_USERNAME;
	-- ALTER TABLE tm_profile DROP COLUMN AO_PASSWORD;
	-- ALTER TABLE tm_profile DROP COLUMN AO_ACCOUNT_NUMBER;

    END$$

DELIMITER ;

call PROC_UPDATE_FOR_MT_PROFILE;

