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
// CatalogWriter.java - Write OASIS Catalog files
// Written by Cornelis Van Der Laan, nils@globalsight.com
// NO WARRANTY! This class is in the public domain.
package com.globalsight.ling.sgml.catalog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * <p>Writes OASIS Open Catalog files.</p>
 *
 * @see Catalog
 */
public class CatalogWriter
{
    /**
     * <p>The debug level</p>
     *
     * <p>In general, higher numbers produce more information:</p>
     * <ul>
     * <li>0, no messages
     * <li>1, minimal messages (high-level status)
     * <li>2, detailed messages
     * </ul>
     */
    public int debug = 0;

    /**
     * <p>Construct a CatalogWriter object.</p>
     */
    public CatalogWriter()
    {
        String property = System.getProperty("xml.catalog.debug");

        if (property != null)
        {
            try
            {
                debug = Integer.parseInt(property);
            }
            catch (NumberFormatException e)
            {
                debug = 0;
            }
        }
    }

    /**
     * <p>Start writing an OASIS Open Catalog file.</p>
     *
     * @param fileUrl The URL or filename of the catalog file to process
     *
     * @throws MalformedURLException Improper fileUrl
     * @throws IOException Error reading catalog file
     */
    public void writeCatalog(String fileUrl, Vector entries)
        throws MalformedURLException, IOException
    {
        URL catalog;

        try
        {
            catalog = new URL(fileUrl);
        }
        catch (MalformedURLException e)
        {
            catalog = new URL(Catalog.FILEPROTOCOL + fileUrl);
        }

        String name = null;
        
        try
        {
            name = catalog.toURI().getPath();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        PrintWriter out = new PrintWriter(new FileOutputStream(new File(name)));

        for (int i = 0, max = entries.size(); i < max; i++)
        {
            CatalogEntry entry = (CatalogEntry)entries.elementAt(i);

            out.println(entry.toString());
        }

        out.close();
    }

    /**
     * <p>Print debug message (if the debug level is high enough).</p>
     *
     * @param level The debug level of this message. This message
     * will only be
     * displayed if the current debug level is at least equal to this
     * value.
     * @param message The text of the message.
     * @param token The catalog file token being processed.
     */
    private void debug(int level, String message, String token)
    {
        if (debug >= level)
        {
            System.out.println(message + ": " + token);
        }
    }

    /**
     * <p>Print debug message (if the debug level is high enough).</p>
     *
     * @param level The debug level of this message. This message
     * will only be
     * displayed if the current debug level is at least equal to this
     * value.
     * @param message The text of the message.
     * @param token The catalog file token being processed.
     * @param spec The argument to the token.
     */
    private void debug(int level, String message, String token, String spec)
    {
        if (debug >= level)
        {
            System.out.println(message + ": " + token + " " + spec);
        }
    }

    /**
     * <p>Print debug message (if the debug level is high enough).</p>
     *
     * @param level The debug level of this message. This message
     * will only be
     * displayed if the current debug level is at least equal to this
     * value.
     * @param message The text of the message.
     * @param token The catalog file token being processed.
     * @param spec1 The first argument to the token.
     * @param spec2 The second argument to the token.
     */
    private void debug(int level, String message, String token,
        String spec1, String spec2)
    {
        if (debug >= level)
        {
            System.out.println(message + ": " + token + " " + spec1);
            System.out.println("\t" + spec2);
        }
    }

    //
    // Test Code
    //

    static public void main(String[] args)
        throws Exception
    {
        Catalog c = new Catalog();

//        c.parseCatalog(Catalog.DEFAULT_CATALOG);
        c.parseCatalog(AmbFileStoragePathUtils.CATALOG_SUB_DIRECTORY);

        String pub = "-//IETF//DTD HTML//EN//3.0";
        String res = c.resolvePublic(pub, null);
        System.err.println("Resolving PUBLIC " + pub + " to URL " + res);

        String res1 = c.resolvePublic("", "tmx14.dtd");
        System.err.println("Resolving SYSTEM tmx14.dtd to URL " + res1);

        System.exit(0);

        pub = "/nils/says/hi";
        try
        {
            c.addPublicId(pub, "abcdefg.hij");
        }
        catch (Exception ex)
        {
            System.err.println("During testing, pubic ID may exist... " +
                ex.getMessage());
        }

        res = c.resolvePublic(pub, null);
        System.err.println("Resolving PUBLIC " + pub + " to URL " + res);

        try
        {
            c.addPublicId(pub, "abcdefg.hij.klm");
            System.err.println("COULD ADD PUBLIC ID AGAIN...");
        }
        catch (Exception ex)
        {
            System.err.println("OK. Could NOT add public id again.");
        }

//        c.writeCatalog(Catalog.DEFAULT_CATALOG);
        c.writeCatalog(AmbFileStoragePathUtils.CATALOG_SUB_DIRECTORY);

        // QA: compare the catalog to the original catalog.
    }
}
