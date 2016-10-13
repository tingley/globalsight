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
import java.util.Formatter;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;

import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Data;
import com.globalsight.ling.tm3.core.TM3DataFactory;
import com.globalsight.ling.tm3.core.TM3SharedTm;
import com.globalsight.ling.tm3.core.TM3Tm;

// Usage:
// show       # shows all 
// show [id]  # detailed info about a since one
@SuppressWarnings("unchecked")
class ShowTmCommand extends TM3Command
{

    @Override
    public String getDescription()
    {
        return "print a list of all TM3 memories";
    }

    @Override
    public String getName()
    {
        return "show";
    }

    @Override
    protected void printExtraHelp(PrintStream out)
    {
        out.println("Displays all TM3 TMs.");
    }

    @Override
    protected boolean requiresDataFactory()
    {
        return true;
    }

    @Override
    protected void handle(CommandLine command) throws Exception
    {
        Formatter f = new Formatter(System.out);

        List<String> args = command.getArgList();
        if (args.size() == 0)
        {
            TM3DataFactory factory = getDataFactory();
            showAll(getManager().getAllTms(factory), f);
        }
        else
        {
            for (String a : args)
            {
                TM3Tm tm = getTm(a);
                if (tm == null)
                {
                    System.err.println("Skipping '" + a + "' - not a valid id");
                    continue;
                }
                showOne(tm, f);
                System.out.println("");
            }
        }
        f.flush();
    }

    private <T extends TM3Data> void showAll(List<TM3Tm<T>> tms, Formatter f)
            throws Exception
    {
        if (tms.size() == 0)
        {
            return;
        }
        f.format("%-12s%s\n", "Id", "Type");
        for (TM3Tm tm : tms)
        {
        	f.format("%-12d%s\n", tm.getId(), tm.getType());
        }
    }

    private void showOne(TM3Tm tm, Formatter f) throws Exception
    {
        f.format("%-12s%d\n", "Id:", tm.getId());
        f.format("%-12s%s\n", "Type:", tm.getType());
        if (tm instanceof TM3SharedTm)
        {
            f.format("%-12s%d\n", "Storage Id:",
                    ((TM3SharedTm) tm).getSharedStorageId());
        }
        Set<TM3Attribute> attrs = tm.getAttributes();
        if (attrs.size() > 0)
        {
            f.format("%s", "Attributes: ");
            for (TM3Attribute attr : attrs)
            {
                f.format("%s ", attr.getName());
            }
            f.format("\n");
        }
    }
}
