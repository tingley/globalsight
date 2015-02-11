package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class EditXMLRule extends BaseTestCase
{
    private XMLRulesFuncs XMLRuleFunc = new XMLRulesFuncs();

    @Test
    public void editXMRule() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.XML_RULES_SUBMENU);

        String ruleName = getProperty("xml.rule.name");
        String newXmlRule = getProperty("xml.rule.edit");
        String description = "This is the new Rule.";

        XMLRuleFunc.editXMLRule(selenium, ruleName, description, newXmlRule);
    }
}
