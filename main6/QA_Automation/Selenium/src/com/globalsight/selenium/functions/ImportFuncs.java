package com.globalsight.selenium.functions;

import org.testng.Reporter;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.pages.Import;
import com.thoughtworks.selenium.Selenium;

public class ImportFuncs extends BasicFuncs {
	/*
	 * Create jobs: before this make sure the files have been copied to the
	 * server to the according folders. Because the selenium can't handle the
	 * Applet now.
	 */

	public void importfiles(Selenium selenium, String ParentFolder) {
		while (selenium.isElementPresent(Import.BackFile_IMG)) {
			selenium.click(Import.BackFile_IMG);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		}

		selenium.click("link=" + ParentFolder);

		int i = 2;
		while (selenium.isElementPresent(Import.AvailableFiles_TABLE + "/tr["
				+ i + "]/td[3]")) {
			String iJobName = selenium.getText(Import.AvailableFiles_TABLE
					+ "/tr[" + i + "]/td[3]");

			selenium.click(Import.AvailableFiles_TABLE + "/tr[" + i
					+ "]/td/input");
			selenium.click(Import.Add_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.click(Import.Next_BUTTON);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

			int j = 1;
			while (selenium.isElementPresent(Import.SelectedFiles_TABLE
					+ "/fieldset[" + j + "]/table/tbody/tr[3]/td/div/div")) {
				selenium.click(Import.SelectedFiles_TABLE + "/fieldset[" + j
						+ "]/table/tbody/tr[3]/td/div/div");
				selenium.click(Import.SelectedFiles_TABLE + "/fieldset[" + j
						+ "]/table/tbody/tr[3]/td[2]/input");
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				j++;
			}

			selenium.click(Import.Next_BUTTON_MAP);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

			selenium.type(Import.JobName_TEXT_FIELD, iJobName);
			selenium.click(Import.ContinueToCreate_CHECKBOX);
			selenium.click(Import.CreateJob_BUTTON_JOB);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			i++;

			Reporter.log("The job " + iJobName
					+ " has been created successfully!");

		}
	}
}
