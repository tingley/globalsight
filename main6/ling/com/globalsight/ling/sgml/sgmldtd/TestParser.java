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
package com.globalsight.ling.sgml.sgmldtd;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import com.globalsight.ling.sgml.GlobalSightDtdParser;
import com.globalsight.ling.sgml.GlobalSightEntity;
import com.globalsight.ling.sgml.catalog.Catalog;
import com.globalsight.util.AmbFileStoragePathUtils;

public class TestParser
{
    static private Catalog s_catalog = new Catalog();
    static
    {
        try
        {
//            s_catalog.parseCatalog(Catalog.DEFAULT_CATALOG);
            s_catalog.parseCatalog(AmbFileStoragePathUtils.CATALOG_SUB_DIRECTORY);
        }
        catch (Exception ex)
        {
            System.err.println("Error initializing SGML catalog: " +
                ex.getMessage());
        }
    }

    public static void main(String[] args)
    {
        try
        {
            GlobalSightDtdParser parser = new DtdParserAdapter();
            parser.setCatalog(s_catalog);

            if (args.length != 1 && args.length != 2)
            {
                System.err.println("Usage: java com.globalsight.ling.sgml.sgmldtd.TestParser [-trace] file.dtd");
                System.exit(1);
            }

            DtdAdapter dtd;

            if (args.length == 1)
            {
                dtd = (DtdAdapter)parser.parseDtd(new URL(args[0]));
            }
            else
            {
                dtd = (DtdAdapter)parser.parseDtd(new URL(args[1]), true);
            }

            ArrayList elements = dtd.getElements();

            for (Iterator iter = elements.iterator(); iter.hasNext();)
            {
                DTDElement element = (DTDElement) iter.next();

                System.out.print(element.getName());
                System.out.println(": ");
                ArrayList atts = dtd.getAttributes(element.getName());
                for (Iterator i2 = atts.iterator(); i2.hasNext();)
                {
                    DTDAttribute attr = (DTDAttribute) i2.next();
                    System.out.print("  ");
                    System.out.print(attr.getName());
                    System.out.print("  ");
                    System.out.println("[" +
                        (attr.getDefaultValue() != null ?
                            attr.getDefaultValue() : "") + "]");
                }

                System.out.println();
            }

            ArrayList entities = dtd.getEntities();

            for (Iterator iter = entities.iterator(); iter.hasNext();)
            {
                GlobalSightEntity entity = (GlobalSightEntity) iter.next();
                System.out.println(entity.getName() + ": " + entity.getValue());
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }
    }
}
