package com.globalsight.everest.edit.offline.download;

import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.util.system.MockSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;

public class DownLoadApiTest
{
    @Before
    public void setup()
    {
        // Install our custom system configuration so that the static intializer
        // doesn't try to touch the database
        HashMap<String, String> map = new HashMap<String, String>();
        
        map.put(SystemConfigParamNames.APPSERVER_VENDOR, "jboss");
        map.put(SystemConfigParamNames.ADD_DELETE_ENABLED, "true");
        
        SystemConfiguration.setDebugInstance(new MockSystemConfiguration(map));
    }

    @After
    public void teardown()
    {
        // Remove the custom SystemConfiguration
        SystemConfiguration.setDebugInstance(null);
    }

    // GBS-1851
    @Test
    public void testGetUniqueExtractedPTFName()
    {
        DownLoadApi api = new DownLoadApi();

        OfflinePageData page1 = new OfflinePageData();
        page1.setPageId("1001");
        page1.setPageName("folder1/test.rtf");
        String uniquePageName1 = api.getUniqueExtractedPTFName(page1,
                AmbassadorDwUpConstants.FILE_EXT_RTF_NO_DOT);

        OfflinePageData page2 = new OfflinePageData();
        page2.setPageId("1002");
        page2.setPageName("folder2/test.rtf");
        String uniquePageName2 = api.getUniqueExtractedPTFName(page2,
                AmbassadorDwUpConstants.FILE_EXT_RTF_NO_DOT);

        OfflinePageData page3 = new OfflinePageData();
        page3.setPageId("1003");
        page3.setPageName("folder3/TeSt.rtf");
        String uniquePageName3 = api.getUniqueExtractedPTFName(page3,
                AmbassadorDwUpConstants.FILE_EXT_RTF_NO_DOT);

        Assert.assertFalse(uniquePageName1.equalsIgnoreCase(uniquePageName2));
        Assert.assertFalse(uniquePageName1.equalsIgnoreCase(uniquePageName3));
        Assert.assertFalse(uniquePageName2.equalsIgnoreCase(uniquePageName3));
        Assert.assertEquals("test.rtf", uniquePageName1);
        Assert.assertEquals("test_1002.rtf", uniquePageName2);
        Assert.assertEquals("TeSt_1003.rtf", uniquePageName3);
    }
}
