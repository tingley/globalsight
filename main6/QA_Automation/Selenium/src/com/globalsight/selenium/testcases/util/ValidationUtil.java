/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.selenium.testcases.util;

import org.testng.Assert;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

/**
 * @author Vincent
 *
 */
public class ValidationUtil extends BaseTestCase
{
    public void verifyWordCount(Selenium selenium, String jobName, String expectedWordCount)
    {
        //Open available my activities list page
        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU, MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);

        //Check if the job was created successfully
        while (!selenium.isElementPresent("link=" + jobName)) {
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        
        //Check word count
        String wordCountStr = selenium.getText("//table[@class='list']/tbody[2]/tr[2]/td[6]");
        Assert.assertEquals(wordCountStr, expectedWordCount);
    }
}
