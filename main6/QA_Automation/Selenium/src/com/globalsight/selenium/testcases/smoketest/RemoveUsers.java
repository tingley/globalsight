package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class RemoveUsers extends BaseTestCase {
	/*
	 * Common variables initialization.
	 */
	private UsersFuncs usersFuncs = new UsersFuncs();

	@Test
	public void removeUsers() throws Exception {
		selenium.click(MainFrame.SETUP_MENU);
		selenium.click(MainFrame.USERS_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.USERS_SUBMENU);

		usersFuncs.removeUsers(selenium,
				getProperty("user.removeUser"));
	}
}
