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
// This software is in the public domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
package com.globalsight.ling.sgml.dtd2;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import com.globalsight.ling.sgml.GlobalSightDtd;
import com.globalsight.ling.sgml.GlobalSightEntity;
import com.globalsight.ling.sgml.catalog.Catalog;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Test code for reading a DTD from the command line and printing out
 * element names and attributes.
 */
public class Test
{
    //**************************************************************************
    // Constants
    //**************************************************************************

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String INDENT = "   ";

    //**************************************************************************
    // Constructors
    //**************************************************************************

    /** Construct a new ClassGenerator. */
    public Test()
    {
    }

    //**************************************************************************
    // Public methods
    //**************************************************************************

    /**
     * Create a set of classes from a DTD.
     *
     * <p>See the introduction for details.</p>
     */
    static public void main(String[] args)
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

        /*
        Enumeration e;

        e = dtd.notations.elements();
        while (e.hasMoreElements())
        {
            Notation n = (Notation)e.nextElement();
            System.out.println("Notation " + n.name +
                " PUBLIC " + n.publicID + " SYSTEM " + n.systemID);
        }

        e = dtd.parsedGeneralEntities.elements();
        while (e.hasMoreElements())
        {
            ParsedGeneralEntity n = (ParsedGeneralEntity)e.nextElement();
            System.out.println("Parsed General Entity " + n.name +
                " = " + n.value);
        }

        e = dtd.unparsedEntities.elements();
        while (e.hasMoreElements())
        {
            UnparsedEntity n = (UnparsedEntity)e.nextElement();
            System.out.println("Unparsed Entity " + n.name +
                " notation = " + n.notation);
        }
     */
    }
}
