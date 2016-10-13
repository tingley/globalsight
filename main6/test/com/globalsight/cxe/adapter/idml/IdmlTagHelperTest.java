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

package com.globalsight.cxe.adapter.idml;

import java.io.File;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.globalsight.everest.util.system.MockEnvoySystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.ClassUtil;
import com.globalsight.util.FileUtil;

public class IdmlTagHelperTest
{
    private IdmlTagHelper help = new IdmlTagHelper();

    @Before
    public void setUp() throws Exception
    {
        SystemConfiguration.setDebugInstance(new MockEnvoySystemConfiguration(
                new HashMap<String, String>()
                {
                    {
                        put("leverager.targetIndexing", "false");
                    }
                }));
    }

    private String getFilePath(String path)
    {
        return IdmlTagHelperTest.class.getResource("source/" + path).getFile();
    }

    @Test
    public void testMergeTags() throws Exception
    {
        String s = FileUtil.readFile(new File(getFilePath("Story_u56df.xml")));
        ClassUtil.testMethod(help, "mergeTags", s);

        s = "<CharacterStyleRange ><Content>this is</Content></CharacterStyleRange><CharacterStyleRange ><Content> a test</Content><Br/></CharacterStyleRange>";
        String expectValue = "<CharacterStyleRange ><Content>this is a test</Content><Br/></CharacterStyleRange>";
        
        String returnValue = (String) ClassUtil
                .testMethod(help, "mergeTags", s);
        
        Assert.assertEquals(expectValue, returnValue);
    }

    @Test
    public void testGetContent() throws Exception
    {
        String s = "<CharacterStyleRange ><Content>this is</Content></CharacterStyleRange> a test";
        String expectValue = "this is";

        String returnValue = (String) ClassUtil.testMethod(help, "getContent",
                s);

        Assert.assertEquals(expectValue, returnValue);
    }

    @Test
    public void testAddFont() throws Exception
    {
        String s1 = "<CharacterStyleRange ><Properties><AppliedFont type=\"string\">Courier New</AppliedFont></Properties><Content>this is</Content></CharacterStyleRange>";
        String s2 = "<CharacterStyleRange ><Properties></Properties><Content>a test</Content></CharacterStyleRange>";
        String expectValue = "<CharacterStyleRange ><Properties><AppliedFont type=\"string\">Courier New</AppliedFont></Properties><Content>a test</Content></CharacterStyleRange>";

        String returnValue = (String) ClassUtil.testMethod(help, "addFont", s1,
                s2);

        Assert.assertEquals(expectValue, returnValue);
    }
}
