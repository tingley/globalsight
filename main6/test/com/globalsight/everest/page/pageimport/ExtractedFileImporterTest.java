/**
 *  Copyright 2009 Welocalize, Inc. 
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

package com.globalsight.everest.page.pageimport;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.everest.util.system.MockSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.ClassUtil;

public class ExtractedFileImporterTest
{
    @Before
    public void init()
    {
        Map<String, String> mm = new HashMap<String, String>();
        mm.put(SystemConfigParamNames.AUTO_REPLACE_TERMS, "False");
        SystemConfiguration.setDebugInstance(new MockSystemConfiguration(mm));
    }

    @Test
    public void testMergePh()
    {
        String s = "<bpt></bpt><ept></ept><ph>1</ph>2<ph>3</ph>4<ph>5</ph><bpt></bpt><ept></ept>6<ph>7</ph><ph>8</ph>9";
        ExtractedFileImporter importer = new ExtractedFileImporter();
        String s2 = (String) ClassUtil.testMethod(importer, "mergePh", s);
        Assert.assertTrue(s2.indexOf("<ph>1</ph>") < 0);
        Assert.assertTrue(s2.indexOf("<ph>3</ph>") > 0);
        Assert.assertTrue(s2.indexOf("<ph>5</ph>") < 0);
        Assert.assertTrue(s2.indexOf("<ph>7</ph>") < 0);
        Assert.assertTrue(s2.indexOf("<ph>8</ph>") < 0);
    }

    @Test
    public void testForGBS2323()
    {
        String s = "<segment segmentId=\"2\" wordcount=\"5\">For more information, click "
                + "<it i=\"43\" pos=\"end\" x=\"3\">&lt;/w:t&gt;&lt;/w:r&gt;</it><bpt i=\"44\" "
                + "type=\"w:hyperlink\">&lt;w:hyperlink w:tooltip=&quot;<sub locType=\"translatable\" "
                + "wordcount=\"1\">here</sub>&quot;&gt;</bpt><bpt i=\"45\" type=\"w:t\">"
                + "&lt;w:r&gt;&lt;w:t&gt;</bpt>here<ept i=\"45\">&lt;/w:t&gt;&lt;/w:r&gt;</ept>"
                + "<ept i=\"44\">&lt;/w:hyperlink&gt;</ept></segment>";
        ExtractedFileImporter importer = new ExtractedFileImporter();
        String s2 = (String) ClassUtil.testMethod(importer, "mergeMultiTags", s);
        Assert.assertEquals(s, s2);
    }
}
