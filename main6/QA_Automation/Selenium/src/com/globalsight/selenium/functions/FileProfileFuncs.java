package com.globalsight.selenium.functions;

import java.util.ArrayList;
import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.FileProfileElements;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class FileProfileFuncs extends BasicFuncs {
   private ArrayList<String> propertyNameArray = new ArrayList<String>();
    
    public void setup(ArrayList<String> propertyNameArray) {
        this.propertyNameArray = propertyNameArray;
    }
	
	/**
	 * Create new file profiles
	 * 
	 * @param selenium
	 * @param testCaseName
	 */
	public void create(Selenium selenium, String testCaseName) {
		String fileProfileNames = ConfigUtil.getDataInCase(testCaseName,
		        propertyNameArray.get(0));
		String localProfiles = ConfigUtil.getDataInCase(testCaseName,
		        propertyNameArray.get(1));
		String sourceFormats = ConfigUtil.getDataInCase(testCaseName,
		        propertyNameArray.get(2));
		String extensions = ConfigUtil.getDataInCase(testCaseName,
		        propertyNameArray.get(3));
        String descriptions = ConfigUtil.getDataInCase(testCaseName,
                propertyNameArray.get(4));
		
		String[] fileProfileNameArray = fileProfileNames.split(",");
		String[] localProfileArray = localProfiles.split(",");
		String[] sourceFormatArray = sourceFormats.split(",");
		String[] extensionArray = extensions.split(",");
		
		String[] descriptionArray = null;
		
		if(descriptions!= null) {
            descriptionArray = descriptions.split(",");
		}

		for (int i = 0; i < fileProfileNameArray.length; i++) {
			clickAndWait(selenium, FileProfileElements.MAIN_NEW_BUTTON);
			selenium.type(FileProfileElements.NEW_NAME_TEXT,
					fileProfileNameArray[i]);

			if (descriptionArray != null && (i < descriptionArray.length - 1))
                selenium.type(FileProfileElements.DESCRIPTION,
                        fileProfileNameArray[i]);
			
			selenium.select(
					FileProfileElements.NEW_LOCALIZATION_PROFILE_SELECT,
					"label=" + localProfileArray[i]);
			selenium.select(FileProfileElements.NEW_SOURCE_FILE_FORMAT_SELECT,
					"label=" + sourceFormatArray[i]);
			selenium.select(FileProfileElements.filterOption_SELECT,
					"label=" + "Choose...");
			selenium.select(FileProfileElements.NEW_ENCODING_SELECT,
					"label=UTF-8");

			String[] tmp = extensionArray[i].split(";");
			for (int j = 0; j < tmp.length; j++) {
				selenium.addSelection(FileProfileElements.NEW_EXTENSION_SELECT,
						"label=" + tmp[j]);
			}
			// save and check
			selenium.click(FileProfileElements.NEW_SAVE_BUTTON);
			if (selenium.isAlertPresent()) {
				Reporter.log(selenium.getAlert());
				Assert.assertTrue(false, selenium.getAlert());
				clickAndWait(selenium, FileProfileElements.NEW_CANCEL_BUTTON);
				continue;
			}
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}
	}

	public void remove(Selenium selenium, String iFileProfileNames)throws Exception {
		
		String fileProfileName[]=iFileProfileNames.split(",");				
		for (String iFileProfileName:fileProfileName)
		{			
			System.out.println( iFileProfileName);
			boolean selected = selectRadioButtonFromTable(selenium, FileProfileElements.MAIN_TABLE, iFileProfileName);
			if (!selected)
			{
				Reporter.log("Cannot find a proper file profile "+iFileProfileName+" to remove.");
				continue;
			}
			else {
				clickAndWait(selenium, FileProfileElements.MAIN_REMOVE_BUTTON);
				Assert.assertEquals((selenium.getConfirmation().equals("Are you sure you want to remove this File Profile?")),true);
			}
			//verify
//			boolean selected2 = selectRadioButtonFromTable(selenium, FileProfileElements.MAIN_TABLE, iFileProfileName);
//			if (!selected2)
//			{
//				Reporter.log("The file profile "+iFileProfileName+" was removed successfully.");
//				continue;
//			}
		}
	}
}
