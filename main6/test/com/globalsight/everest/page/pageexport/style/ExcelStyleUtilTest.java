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
package com.globalsight.everest.page.pageexport.style;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.globalsight.util.ClassUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.FileUtilTest;

public class ExcelStyleUtilTest
{
    private File file = null;
    private FileUtilTest fileUtilTest = new FileUtilTest();

    @Test
    public void testRemoveXmlnsAttribute() throws IOException
    {
        file = fileUtilTest.getTestFile(ExcelStyleUtilTest.class, "testdata/sharedStrings.xml");
        String original = FileUtil.readFile(file, "utf-8");
        Assert.assertTrue(original.contains("xmlns=\"\""));

        ClassUtil.testMethod(new ExcelStyleUtil(), "removeXmlnsAttribute", file.getAbsolutePath());
        String result = FileUtil.readFile(file, "utf-8");

        Assert.assertFalse(result.contains("xmlns=\"\""));
        // update file content back to original
        FileUtil.writeFile(file, original, "utf-8");
    }
}
