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
package com.globalsight.ling.tm3.tools;

import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Tm;

@SuppressWarnings("static-access")
class DeleteAttributeCommand extends TM3Command
{

    @Override
    public String getDescription()
    {
        return "remove attributes from a TM";
    }

    @Override
    public String getName()
    {
        return "delete-attr";
    }

    @Override
    protected String getUsageLine()
    {
        return getName() + " [options] -" + TM
                + " tmid attr-name1 ... attr-nameN";
    }

    @Override
    protected void printExtraHelp(PrintStream out)
    {
        out.println("Deletes one or more attributes from a given TM");
    }

    static final String TM = "tm";
    static final Option TM_OPT = OptionBuilder.withArgName("id").hasArg()
            .withDescription("TM id").isRequired().create(TM);

    @Override
    public Options getOptions()
    {
        return getDefaultOptions().addOption(TM_OPT);
    }

    @Override
    protected void handle(CommandLine command) throws Exception
    {
        TM3Tm<?> tm = getTm(command.getOptionValue(TM));
        if (tm == null)
        {
            die("Not a valid TM id: '" + command.getOptionValue(TM) + "'");
        }
        for (String name : command.getArgs())
        {
            TM3Attribute attr = tm.getAttributeByName(name);
            if (attr == null)
            {
                System.out.println("Not an attribute: '" + name + "'");
                continue;
            }
            tm.removeAttribute(attr);
            System.out.println("Removed '" + attr + "'");
        }
    }

}
