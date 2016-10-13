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
package com.globalsight.ling.sgml.dtd;

import com.globalsight.ling.sgml.GlobalSightDtd;
import com.globalsight.ling.sgml.GlobalSightEntity;
import com.globalsight.ling.sgml.catalog.Catalog;
import com.globalsight.util.AmbFileStoragePathUtils;

import java.io.*;
import java.util.*;
import java.net.URL;

/** Example program to read a DTD and print out its object model
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2009/04/14 15:09:38 $ by $Author: yorkjin $
 */

public class Test
{
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Syntax: java ClassGenerator <dtd-file>");
            return;
        }

        Test test = new Test();
        try
        {
            File file = new File(args[0]);
            URL url = new URL("file:///" + file.getAbsolutePath());
            test.testDTD(url);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void testDTD(URL p_url)
        throws Exception
    {
        DTDParser parser = new DTDParser();

        Catalog catalog = new Catalog();
//        catalog.parseCatalog(Catalog.DEFAULT_CATALOG);
        catalog.parseCatalog(AmbFileStoragePathUtils.CATALOG_SUB_DIRECTORY);
        parser.setCatalog(catalog);

        // Parse the DTD and ask the parser to guess the root element
        GlobalSightDtd dtd = parser.parseDtd(p_url);

        System.out.println("Parsed DTD " + p_url);
        processElementTypes(dtd);
        processEntities(dtd);
    }

    private void processElementTypes(GlobalSightDtd p_dtd)
    {
        ArrayList elems = p_dtd.getElementNames();
        for (int i = 0, max = elems.size(); i < max; i++)
        {
            processElement(p_dtd, (String)elems.get(i));
        }
    }

    private void processElement(GlobalSightDtd p_dtd, String p_elem)
    {
        System.out.print("Element ");
        System.out.println(p_elem);

        // Process the attributes, adding one property for each.
        processAttributes(p_dtd, p_elem);
    }

    private void processAttributes(GlobalSightDtd p_dtd, String p_elem)
    {
        ArrayList attrs = p_dtd.getAttributeNames(p_elem);

        for (int i = 0, max = attrs.size(); i < max; i++)
        {
            String attr = (String)attrs.get(i);

            System.out.println("\tAttribute " + attr);
        }
    }

    private void processEntities(GlobalSightDtd p_dtd)
    {
        ArrayList ents = p_dtd.getEntities();

        for (int i = 0, max = ents.size(); i < max; i++)
        {
            GlobalSightEntity ent = (GlobalSightEntity)ents.get(i);

            System.out.println("Entity " + ent.getName() + " = " + ent.getValue());
        }
    }
}
