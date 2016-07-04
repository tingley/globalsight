package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.MTProfile;
import com.globalsight.selenium.testcases.util.SeleniumUtils;
import com.thoughtworks.selenium.Selenium;

/*
 * FileName: MTProfileFuncs.java
 * Author:Erica
 * Methods: MTProfilenew() 
 * 
 * History:
 * Date       Comments       Updater
 * 2016-5-15   First Draft    Erica
 */

public class MTProfileFuncs extends BasicFuncs {
	private SeleniumUtils iSelniumUtils = new SeleniumUtils();


	/*
	 * Create a new MT Profile, but first check if the MT Profile is exists. If the MT
	 * Profile has already exists. Click "Cancel" an back to the MT Profile page.
	 * This is for MT_Engin is MS_Translator,
	 */
	
	
	public void newMTProfile_MSMT(Selenium selenium, String mt_Profile_Name,	
		String description,
		String mT_Threshold_Level,
		String ignore_TM_Matches_for_MT,	
		String log_Debug_Info,
		String include_MT_Identifiers,
		String leading,	
		String trailing,
		String mS_Translator_URL,
		String client_ID,
		String client_Secret,	
		String category,
		String serbian_Serbia_sr_RS,
		String serbian_YU_sr_YU,	
		String translation_mode,	
		String max_Chunk_Length
		)
    {
		selenium.type(MTProfile.MTP_SEARCH_CONTENT_TEXT, mt_Profile_Name);
    	selenium.keyDown(MTProfile.MTP_SEARCH_CONTENT_TEXT, "\\13");
    	selenium.keyUp(MTProfile.MTP_SEARCH_CONTENT_TEXT, "\\13");
//    	selenium.waitForFrameToLoad("css=table.listborder", "1000");

   	 if (!(selenium.isElementPresent("link=" + mt_Profile_Name))){
    	clickAndWait(selenium, MTProfile.MTP_NEW_BUTTON);
        selenium.type(MTProfile.MTP_NAME_TEXT, mt_Profile_Name);
        selenium.type(MTProfile.MTP_DESCRIPTION_TEXT, description);
        selenium.select(MTProfile.MTP_ENGINE_SELECT, MTProfile.MTP_ENGINE_MSMT_lABEL);
        
        if (!((mT_Threshold_Level.isEmpty()) || (mT_Threshold_Level.equalsIgnoreCase("x")) || 
        		(mT_Threshold_Level.equalsIgnoreCase("o"))))
        	selenium.type(MTProfile.MTP_CONFIDENCE_SCORE_TEXT, mT_Threshold_Level);
        
        if (log_Debug_Info.equalsIgnoreCase("true"))
        	selenium.check(MTProfile.MTP_LOG_DEBUG_INFO_CHECKBOX);
        else if (log_Debug_Info.equalsIgnoreCase("false"))
        	selenium.uncheck(MTProfile.MTP_LOG_DEBUG_INFO_CHECKBOX);
        	
        if (include_MT_Identifiers.equalsIgnoreCase("true"))
        {
        	selenium.check(MTProfile.MTP_LOG_DEBUG_INFO_CHECKBOX);
        	selenium.type(MTProfile.MTP_IDENTIFIER_LEADING_TEXT, leading);
        	selenium.type(MTProfile.MTP_IDENTIFIER_TRAILING_TEXT, trailing);
        }
        else if (include_MT_Identifiers.equalsIgnoreCase("false"))
        	selenium.uncheck(MTProfile.MTP_LOG_DEBUG_INFO_CHECKBOX);
        
        selenium.type(MTProfile.MSMT_URL_TEXT, mS_Translator_URL);
        
        if (ignore_TM_Matches_for_MT.equalsIgnoreCase("true")) 
        {
        	if (mT_Threshold_Level.equals("100")) selenium.check(MTProfile.MTP_IGNORE_TM_MATCHES_CHECKBOX);
        }
        	
        selenium.type(MTProfile.MSMT_CLIENT_ID_TEXT, client_ID);
        selenium.type(MTProfile.MSMT_CLIENT_SECRET_TEXT, client_Secret);
        selenium.type(MTProfile.MSMT_CATEGORY_TEXT, category);
        
        if (serbian_Serbia_sr_RS.equalsIgnoreCase(MTProfile.MSMT_SR_LATN_LABEL))
        	selenium.select(MTProfile.MSMT_SR_RS_SELECT, MTProfile.MSMT_SR_LATN_LABEL);
        else if (serbian_Serbia_sr_RS.equalsIgnoreCase(MTProfile.MSMT_SR_LYRL_LABEL))
        	selenium.select(MTProfile.MSMT_SR_RS_SELECT, MTProfile.MSMT_SR_LYRL_LABEL);
        
        if (serbian_YU_sr_YU.equalsIgnoreCase(MTProfile.MSMT_SR_LATN_LABEL))
        	selenium.select(MTProfile.MSMT_SR_YU_SELECT, MTProfile.MSMT_SR_LATN_LABEL);
        else if (serbian_YU_sr_YU.equalsIgnoreCase(MTProfile.MSMT_SR_LYRL_LABEL))
        	selenium.select(MTProfile.MSMT_SR_YU_SELECT, MTProfile.MSMT_SR_LYRL_LABEL);
        
        if (translation_mode.equals("1"))
        	selenium.click(MTProfile.MSMT_SEND_TEXT_BETWEEN_TAGS_RADIO_BUTTON);
        else if (translation_mode.equals("2"))
        	selenium.click(MTProfile.MSMT_SEND_TEXT_INCLUDING_TAGS_RADIO_BUTTON);
        
        if (!((max_Chunk_Length.isEmpty()) || (max_Chunk_Length.equalsIgnoreCase("x")) || 
        		(max_Chunk_Length.equalsIgnoreCase("o"))))
        	selenium.type(MTProfile.MSMT_MAX_CHUNK_LENGTH_TEXT, max_Chunk_Length);
        	
    	
    	selenium.click(MTProfile.MTP_SAVE_BUTTON);
        if (selenium.isAlertPresent()) 
            selenium.getAlert();
        
   }
  }
	
}
