/**
 *  Copyright 2009, 2012 Welocalize, Inc. 
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

package com.globalsight.selenium.testcases.office2010;

import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.util.SeleniumTestHelper;

import org.testng.Assert;
import org.testng.annotations.Test;;
/**
 * Automation test case for GBS-2305
 * missing spaces in final exported file office 2010 filter
 * 
 * Reporter         Andrew Gibbons
 * Assignee         Wayne Zou
 * Affects version  None
 * Fix version      8.2.1
 *
 * @author Vincent Yan
 * @date 2012/02/07
 */

@Test (groups = {"all", "GBS2305"})
public class GBS2305Test extends BaseTestCase
{
    FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
    boolean usingExist = false;
    
    @Test
    public void testGBS2305() throws Exception {
		String usingExistStr = getDataInCase("usingExist");
		usingExist = Boolean.parseBoolean(usingExistStr);

		FileProfile fileProfile = fileProfileFuncs.getFileProfileInfo(testCaseName);
		SeleniumTestHelper.addFileProfile(selenium, fileProfile, usingExist);

		String jobName = getDataInCase("jobName");
		String filePaths = getDataInCase("filePaths");
		String fileProfiles = getDataInCase("fileProfiles");
		String targetLocales = getDataInCase("targetLocales");

		String createdJobName = SeleniumTestHelper.createJob(jobName, filePaths, fileProfiles, targetLocales);

		String expectedWordCount = getDataInCase("expectedWordCount");
		Assert.assertTrue(SeleniumTestHelper.verifyWordCount(selenium, createdJobName, expectedWordCount));

		String workflow = getDataInCase("workflow");
		SeleniumTestHelper.exportJob(selenium, createdJobName, workflow);

		SeleniumTestHelper.downloadAndCompare(createdJobName, filePaths);
	}
}
