package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateXMLRule.java
 * Author:Jester
 * Tests:Create_XMLRule()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-7  First Version  Jester
 */

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateXMLRule extends BaseTestCase
{
    private XMLRulesFuncs XMLRulesFuncs = new XMLRulesFuncs();

    @Test
    public void createXMLRule() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.XML_RULES_SUBMENU);

        XMLRulesFuncs.newXMLRule(selenium, getProperty("xml.rule.name"),
                getProperty("xml.rule"));
    }
}
