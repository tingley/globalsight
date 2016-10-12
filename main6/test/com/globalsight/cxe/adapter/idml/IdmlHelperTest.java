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

import com.globalsight.cxe.entity.filterconfiguration.InddFilter;
import com.globalsight.everest.util.system.MockEnvoySystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.ClassUtil;
import com.globalsight.util.FileUtil;

public class IdmlHelperTest
{
    private IdmlHelper helper = new IdmlHelper();
    private static String IDML_SOURCE = "test.idml";
    private static final String LINE_BREAK = FileUtil.unUnicode("\u2028");
    private static final String NONBREEAKING_SPACE = FileUtil.unUnicode("\u00A0");
    
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
        return IdmlHelperTest.class.getResource("source/" + path).getFile();
    }
    
    @Test
    public void testFormatForImport() throws Exception
    {
        String s = FileUtil.readFile(new File(getFilePath("Story_u56df.xml")));
        helper.formatForImport(s);
    }
    
    @Test
    public void testConvert() throws Exception
    {
        String file = getFilePath(IDML_SOURCE);
        ClassUtil.testMethod(helper, "convert", file);
    }
    
    
    @Test
    public void testIntegrate() throws Exception
    {
        String file = getFilePath(IDML_SOURCE);
        ClassUtil.testMethod(helper, "integrate", file);
    }
       
    @Test
    public void testOptimizeForOddChar() throws Exception
    {
        InddFilter filter = new InddFilter();
        filter.setReplaceNonbreakingSpace(true);
        filter.setExtractLineBreak(false);
        
        ClassUtil.updateField(helper, "filter", filter);
        
        String s = "this" + LINE_BREAK + " is" + NONBREEAKING_SPACE + "test";
        String returnValue = (String) ClassUtil.testMethod(helper, "optimizeForOddChar", s);
        String expectValue = "this is test";
        
        Assert.assertEquals(expectValue, returnValue);
    }
}
