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

package com.globalsight.selenium.testcases.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jodd.util.StringUtil;

import com.globalsight.selenium.testcases.ConfigUtil;

/**
 * Utility class for file compare
 * The compare progress invokes BeyondCompare tool to compare files which are 
 * stored in 'Base_Path' and 'Base_Path_Result' properties in configuration.
 * 
 * To use this class, you need to add the path of BeyondCompare tool to system
 * PATH variable.
 *  
 * @author  Vincent Yan 
 * @date    2011/12/18
 * @version 1.0
 * @since   8.2.2
 */
public class CompareUtil
{
    public static void generateCompareReport(String filePaths) throws IOException
    {
        String baseInputPath = ConfigUtil.getConfigData("Base_Path");
        String baseOutputPath = ConfigUtil.getConfigData("Base_Path_Result");
        String comparePath = ConfigUtil.getConfigData("Compare_Path");

        String baseCmd = "cmd.exe /c bcompare.exe @\"" + comparePath
                + "\\CompareScript.txt\" ";

        String[] files = filePaths.split(",");
        for (String file : files) {
            file = StringUtil.replace(file, " ", "");
            
            String source = baseInputPath + file;
            String target = baseOutputPath + file;
            String report = comparePath + file + "_Report.html";

            String cmd = baseCmd + "\"" + source + "\" \"" + target + "\" " + report + "\"";
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            while ((reader.readLine()) != null)
            {
            }

            reader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            while ((reader.readLine()) != null)
            {
            }
            if (process.exitValue() == 1)
            {
            }
        }
    }
}
