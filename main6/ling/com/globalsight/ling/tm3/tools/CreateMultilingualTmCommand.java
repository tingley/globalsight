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

import com.globalsight.ling.tm3.core.TM3Tm;

public class CreateMultilingualTmCommand extends CreateTmCommand
{

    @Override
    protected TM3Tm<?> createTm(CommandLine command) throws Exception
    {
        return getManager().createMultilingualTm(null, Collections.EMPTY_SET);
    }

    @Override
    public String getDescription()
    {
        return "create a multilingual TM with dedicated storage";
    }

    @Override
    public String getName()
    {
        return "create-multilingual";
    }

    @Override
    protected void printExtraHelp(PrintStream out)
    {
        out.println("Creates a new multilingual TM with dedicated storage.");
    }

}
