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
import java.util.Collections;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.globalsight.ling.tm3.core.TM3Tm;

class CreateSharedTmCommand extends CreateTmCommand
{

    @Override
    protected TM3Tm<?> createTm(CommandLine command) throws Exception
    {
        String s = command.getOptionValue(STORAGE);
        int i = Integer.valueOf(s);
        return getManager().createMultilingualSharedTm(null,
                Collections.EMPTY_SET, i);
        // (Integer)command.getParsedOptionValue(STORAGE));
    }

    static final String STORAGE = "storage";
    @SuppressWarnings("static-access")
    static final Option STORAGE_OPT = OptionBuilder.withArgName("id").hasArg()
            .withDescription("using shared storage pool <id>").isRequired()
            .withType(Integer.class).create(STORAGE);

    @Override
    public Options getOptions()
    {
        Options opts = getDefaultOptions();
        opts.addOption(STORAGE_OPT);
        return opts;
    }

    @Override
    public String getDescription()
    {
        return "create a multilingual TM with shared storage";
    }

    @Override
    public String getName()
    {
        return "create-shared";
    }

    @Override
    protected String getUsageLine()
    {
        return getName() + " [options] -" + STORAGE + " storageId";
    }

    @Override
    protected void printExtraHelp(PrintStream out)
    {
        out.println("Creates a new multilingual TM using a shared storage pool.");
    }

}
