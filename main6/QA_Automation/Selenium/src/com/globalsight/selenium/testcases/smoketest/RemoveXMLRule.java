package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class RemoveXMLRule extends BaseTestCase
{
    private XMLRulesFuncs xmlRulesFuncs = new XMLRulesFuncs();

    @Test
    public void remove() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.XML_RULES_SUBMENU);

        String ruleName = getProperty("xml.rule.name");
        xmlRulesFuncs.removeRule(selenium, ruleName);
    }
}
