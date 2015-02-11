package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: TestXMLDTD.java
 * Author:Jester
 * Tests:Create_XMLDTD(),Remove_XMLDTD()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-1  First Version  Jester
 */
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.XMLDTDSFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TestXMLDTD extends BaseTestCase
{

    /*
     * Common Variables
     */
    XMLDTDSFuncs XMLDTDSFuncs = new XMLDTDSFuncs();

    @Test
    public void createXMLDTD() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.XMLDTDS_SUBMENU);

        XMLDTDSFuncs.newXMLDTD(selenium, getProperty("xml.dtd"));
    }

    @Test
    public void removeXMLDTD() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.XMLDTDS_SUBMENU);

        XMLDTDSFuncs.XMLDTDRemove(selenium, getProperty("xml.dtd.name"));
    }
}
