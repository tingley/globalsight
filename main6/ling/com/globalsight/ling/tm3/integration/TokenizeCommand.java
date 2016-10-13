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
package com.globalsight.ling.tm3.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import com.globalsight.ling.tm3.tools.TM3Command;
import com.globalsight.util.GlobalSightLocale;

public class TokenizeCommand extends TM3Command
{

    @Override
    public String getDescription()
    {
        return "tokenizes";
    }

    @Override
    public String getName()
    {
        return "tokenize";
    }

    @Override
    public boolean requiresDataFactory()
    {
        return true;
    }

    @Override
    protected void handle(CommandLine command) throws Exception
    {
        GlobalSightLocale locale = (GlobalSightLocale) getDataFactory()
                .getLocaleById(32);
        if (locale == null)
        {
            System.out.println("No such locale");
            System.exit(1);
        }
        List<String> args = command.getArgList();
        if (args.size() == 0)
        {
            System.out.println("no filename");
            System.exit(1);
        }

        File f = new File(args.get(0));
        if (!f.exists())
        {
            System.out.println("File '" + f + "' does not exist");
            System.exit(1);
        }

        BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), "UTF-8"));
        for (String line = r.readLine(); line != null; line = r.readLine())
        {
            System.out.println("Args: " + line);
            GSTuvData data = new GSTuvData(line, locale);
            for (String s : data.getTokens())
            {
                System.out.print("[" + s + "]");
                if (s.length() == 1)
                {
                    System.out.print(" (" + (int) s.charAt(0) + ")");
                }
                System.out.println("");
            }
        }

    }

}
