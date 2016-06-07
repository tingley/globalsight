package com.globalsight.selenium.functions;

/*
 * FileName: CompanyFuncs.java
 * Author:Jester
 * Methods: CompanyNew() 
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-23  First Version  Jester
 */

import org.testng.Assert;

import com.globalsight.selenium.pages.Company;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class CompanyFuncs extends BasicFuncs
{

    /**
     * Create a New Company with the name provided. If the Company already
     * exits, click "Cancel" and back to the companies page.
     */
    public void newCompany(Selenium selenium, String testCaseName)
            throws Exception
    {
        clickAndWait(selenium, Company.New_BUTTON);
        String iCompanyName = ConfigUtil.getConfigData("company");
        String enableIpFilter = ConfigUtil.getDataInCase(testCaseName,
                "enableIpFilter");
        selenium.type(Company.Name_TEXT_FIELD, iCompanyName);
        if (enableIpFilter.equals("no"))
        {
            selenium.uncheck(Company.ENABLE_IP_FILTER);
        }
        else 
        {
            selenium.check(Company.ENABLE_IP_FILTER);
        }
//        selenium.click(Company.Next_BUTTON);
//        if(selenium.isAlertPresent())
//        { 
//            clickAndWait(selenium, Company.Cancel_BUTTON);           
//         }
//        else
//        {   
            selenium.click(Company.Save_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);           
//         }
            
        selenium.type(Company.Name_Filter_ID, iCompanyName);
        selenium.keyDown(Company.Name_Filter_ID, "13");
        selenium.keyUp(Company.Name_Filter_ID, "13");
      
        Assert.assertEquals(this.isElementPresent(selenium,
                "link=" + iCompanyName),
                true);
    }
}
