package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.AttributeGroupsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class RemoveAttributeGroups extends BaseTestCase
{
    private AttributeGroupsFuncs attributesGroupFuncs = new AttributeGroupsFuncs();

    @Test
    public void removeAttributeGroups() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.ATTRIBUTE_GROUPS_SUBMENU);
        
        attributesGroupFuncs.removeAttributesGroup(selenium,
                getProperty("attributeGroup.newName"));
    }
}
