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
package com.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * A util class, provide some methods to run system command.
 * 
 */
public class CmdUtil
{
    private static Logger log = Logger.getLogger(CmdUtil.class);

    /**
     * Excute system command.
     * 
     * @param cmd
     *            The system command.
     * @throws Exception
     *             Throwed out if excute failed.
     */
    public static List<String> run(String[] cmd) throws Exception
    {
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader in = null;
        BufferedReader sin = null;

        List<String> result = new ArrayList<String>();
        try
        {
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));

            sin = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line;

            while ((line = in.readLine()) != null)
            {
                System.out.println(line);
                result.add(line);
            }

            while ((line = sin.readLine()) != null)
            {
                log.info(line);
                if (line.indexOf(":") > 0)
                {
                    line = line.substring(line.indexOf(":") + 1);
                }
                if (line.indexOf("(") > 0)
                {
                    line = line.substring(0, line.lastIndexOf("("));
                }

                line = line.trim();
                if (line.length() > 0)
                {

                    if (!line.endsWith("."))
                    {
                        line += ".";
                    }

                    throw new Exception(line);
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
            if (sin != null)
            {
                sin.close();
            }
        }

        return result;
    }

}
