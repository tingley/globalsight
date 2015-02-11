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

import org.junit.Assert;
import org.junit.Test;

import com.globalsight.util.ClassUtil;

public class ExtractedFileImporterTest
{

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
}
