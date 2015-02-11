package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.VendorPOReportWebForm;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.ConfigUtil;

public class VendorPOReport extends BaseTestCase {
	@Test
	public void generateReport() {
		selenium.click(MainFrame.REPORTS_MENU);
		selenium.click(MainFrame.REPORTS_MAIN_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click(VendorPOReportWebForm.REPORT_LINK);

		selenium.waitForPopUp(VendorPOReportWebForm.POPUP_WINDOW_NAME, CommonFuncs.SHORT_WAIT);
		selenium.selectWindow("name=" + VendorPOReportWebForm.POPUP_WINDOW_NAME);

		initOptions();

		selenium.click(VendorPOReportWebForm.SUBMIT_BUTTON);

		// Wait for the download progress finish.
		try {
			Thread.sleep((long) 10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Verify the file exists or not
		FileRead fileRead = new FileRead();
		File file = fileRead.getFile(VendorPOReportWebForm.REPORT_FILE_NAME);
		Assert.assertTrue(file.exists());
		// Moved the file to the sub folder.
		fileRead.moveFile(file);
	}

	/**
	 * Init the options of the report
	 */
	private void initOptions() {
		String className = getClass().getName();

		String[] projects = ConfigUtil.getDataInCase(className, "project").split(",");
		String[] jobStatus = ConfigUtil.getDataInCase(className, "jobStatus").split(",");
		String[] targetLocales = ConfigUtil.getDataInCase(className, "targetLocale").split(",");
		String startTime = ConfigUtil.getDataInCase(className, "startTime");
		String startTimeUnits = ConfigUtil.getDataInCase(className, "startTimeUnits");
		String endsTime = ConfigUtil.getDataInCase(className, "endsTime");
		String endsTimeUnits = ConfigUtil.getDataInCase(className, "endsTimeUnits");
		String currency = ConfigUtil.getDataInCase(className, "currency");
		String recal = ConfigUtil.getDataInCase(className, "recal");

		selenium.removeSelection(VendorPOReportWebForm.PROJECTS_SELECTOR, "label=<ALL>");
		selenium.removeSelection(VendorPOReportWebForm.JOBSTATUS_SELECTOR, "label=<ALL>");
		selenium.removeSelection(VendorPOReportWebForm.TARGETLOCALE_SELECTOR, "label=<ALL>");

		// Projects
		for (int i = 0; i < projects.length; i++) {
			selenium.addSelection(VendorPOReportWebForm.PROJECTS_SELECTOR, "label=" + projects[i]);
		}
		// Job Status
		for (int i = 0; i < jobStatus.length; i++) {
			selenium.addSelection(VendorPOReportWebForm.JOBSTATUS_SELECTOR, "label=" + jobStatus[i]);
		}
		// Target Locales
		for (int i = 0; i < targetLocales.length; i++) {
			selenium.addSelection(VendorPOReportWebForm.TARGETLOCALE_SELECTOR, "label=" + targetLocales[i]);
		}
		// Date Range
		selenium.type(VendorPOReportWebForm.STARTSTIME, startTime);
		selenium.select(VendorPOReportWebForm.STARTSTIMEUNITS, "label=" + startTimeUnits);
		selenium.type(VendorPOReportWebForm.ENDSTIME, endsTime);
		selenium.select(VendorPOReportWebForm.ENDSTIMEUNITS, "label=" + endsTimeUnits);
		// Currency
		selenium.select(VendorPOReportWebForm.CURRENCY, "label=" + currency);
		// Re-caculate
		selenium.select(VendorPOReportWebForm.Re_CALCULATE, "label=" + recal);
	}
}
