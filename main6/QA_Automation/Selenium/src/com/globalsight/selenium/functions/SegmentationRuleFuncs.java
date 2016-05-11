package com.globalsight.selenium.functions;

/*
 * FileName: SegmentationRuleFuncs.java
 * Author:Erica
 */

import java.io.File;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.SegmentationRule;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class SegmentationRuleFuncs extends BasicFuncs {
	
	public void uploadSRXRule(Selenium selenium, String sRXRuleName, String uploadFilePath) 
			throws Exception {

		clickAndWait(selenium, SegmentationRule.UPLOAD_BUTTON);
		selenium.type(SegmentationRule.UPLOAD_FILE_PATH_TEXT, uploadFilePath);
		clickAndWait(selenium, SegmentationRule.UPLOAD_FILE_BUTTON);
		selenium.type(SegmentationRule.SEGMENTATION_RULE_NAME_TEXT, sRXRuleName);
		selenium.click(SegmentationRule.SEGMENTATION_RULE_VALIDATE_BUTTON);
		 if (selenium.isAlertPresent()) 
	            selenium.getAlert();
	        if (selenium.isTextPresent(SegmentationRule.SEGMENTATION_RULE_VALIDATE_ALERT))
	        	selenium.close();
		selenium.click(SegmentationRule.SEGMENTATION_RULE_SAVE_BUTTON);
	}

}
