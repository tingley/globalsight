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
package test.HtmlExtractor;

import test.Arguments;
import test.FileListBuilder;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;

import com.globalsight.ling.common.XmlWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.DiplomatAttribute;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.DiplomatReader;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWordCounter;
import com.globalsight.ling.docproc.extractor.html.Extractor;

import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class HtmlSegmenter
    implements org.xml.sax.ErrorHandler,
               org.xml.sax.EntityResolver
{
    public static final String DIPLOMAT_DTD = "/dtds/diplomat.dtd";
    public static final String DIPLOMAT_DTD_URL = 
        "http://globalsight.com/dtds/diplomat.dtd";
    public static final String DIPLOMAT_DOCTYPE = 
        "<!DOCTYPE diplomat SYSTEM \"" + DIPLOMAT_DTD_URL + "\">";

    public static void main(String argv[])
    {
        boolean b_validate = false;
        String str_fileName;
        URL url_fileName;

        try
        {
            Arguments getopt = new Arguments ();
            int c;

            getopt.setUsage(new String[] 
                    { 
                    "Usage: java test.HtmlSegmenter [-h] [-v] file",
                    "Converts an HTML file to Diplomat XML and runs the segmenter.",
                    "The result is written to stdout unless the -v option is used.",
                    "\t-v: validate the result against the Diplomat DTD.",
                    "\t-h: show this help."
                    } );
            

            getopt.parseArgumentTokens(argv, new char[] {});
            while ((c = getopt.getArguments()) != -1)
            {
                switch (c)
                {
                    case 'v':
                        b_validate = true;
                        break;
                    case 'h':
                    default:
                        getopt.printUsage();
                        System.exit(1);
                        break;
                }
            }
            
            str_fileName = getopt.getlistFiles();
            if (str_fileName == null)
            {
                getopt.printUsage();
                System.exit(1);
            }

            url_fileName = fileToURL(str_fileName);
            System.err.println("Extracting " + url_fileName.toString());

            // Read in HTML file and create Input object
            EFInputData input = new EFInputData ();
            input.setCodeset("8859_1");
            Locale locale = new Locale ("en", "US");
            input.setLocale(locale);
            input.setURL(url_fileName.toString());

            // Extraction
            Output output = new Output();
            Extractor extractor = new Extractor();
            extractor.init(input, output);
            extractor.loadRules();
            extractor.extract();

            // Fetch result
            String strTMX = DiplomatWriter.WriteXML(output);

            String strSegmentedTMX = strTMX;

            // bug: DiplomatWordCounter is broken (segments not counted,
            // output not escaped).

            System.err.println("BUG: counting words first");

            // So we add the word counts first (translatable only)
            DiplomatWordCounter wc = new DiplomatWordCounter ();
            strSegmentedTMX = wc.countDiplomatDocument(strSegmentedTMX);

            // then segment result
            DiplomatSegmenter ds = new DiplomatSegmenter ();
            strSegmentedTMX = ds.segment(strSegmentedTMX);

            if (b_validate)
            {
                HtmlSegmenter object = new HtmlSegmenter ();
                object.validateDiplomatXML(strSegmentedTMX);
            }
            else
            {
                System.out.println(strSegmentedTMX);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }


    public void validateDiplomatXML(String str_input)
    {
        try
        {
            String str_xml;
            int n_firstLine;

            System.err.println("HACK: inserting DOCTYPE into diplomat");
            n_firstLine = str_input.indexOf('\n');

            str_xml = str_input.substring(0,n_firstLine) + DIPLOMAT_DOCTYPE + 
              str_input.substring(n_firstLine);

            DOMParser parser = new DOMParser ();
            parser.setFeature("http://xml.org/sax/features/validation", true);

            parser.setEntityResolver(this);
            parser.setErrorHandler(this);
        
            parser.parse(new InputSource (new StringReader (str_xml)));

            Document document = parser.getDocument();

            System.err.println("Diplomat XML has been validated juuust fine.");
        }
        catch (Exception e)
        {
            System.err.println("Diplomat XML could not be validated");
            e.printStackTrace();
        }
    }
    

    //
    // Implementation of interface org.xml.sax.EntityResolver
    //
    public InputSource resolveEntity (String publicId, String systemId)
        throws SAXException, IOException
    {
        if (systemId.equals(DIPLOMAT_DTD_URL)) 
        {
            // return a special input source
            InputStream stream = 
              HtmlSegmenter.class.getResourceAsStream(DIPLOMAT_DTD);

            if (null == stream)
            {
                throw new IOException ("Cannot find diplomat dtd " + DIPLOMAT_DTD);
            }

            System.err.println ("using dtd " + DIPLOMAT_DTD);
            
            return new InputSource (stream);
        } 

        // else use the default behaviour
        return null;
    }


    //
    // Implementation of interface org.xml.sax.ErrorHandler 
    //
    public void warning(SAXParseException ex) 
    {
        System.err.println("[Warning] " +
          getLocationString(ex) + ": " + ex.getMessage());
    }

    public void error(SAXParseException ex) 
        throws SAXParseException
    {
        System.err.println("[Error] " +
          getLocationString(ex) + ": " + ex.getMessage());
        throw ex;
    }

    public void fatalError(SAXParseException ex) 
        throws SAXParseException 
    {
        System.err.println("[Fatal Error] " +
          getLocationString(ex) + ": " + ex.getMessage());
        throw ex;
    }

    /** Returns a string of the location. */
    private String getLocationString(SAXParseException ex) 
    {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) 
        {
            int index = systemId.lastIndexOf('/');
            if (index != -1) 
                systemId = systemId.substring(index + 1);
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();
    }


    //
    // static helper methods
    //

    static private URL fileToURL(String sfile) 
        throws Exception
    {
        File file = new File (sfile);
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");

        if (fSep != null && fSep.length() == 1)
        {
            path = path.replace(fSep.charAt(0), '/');
        }
    
        if (path.length() > 0 && path.charAt(0) != '/')
        {
            path = '/' + path;
        }

        System.err.println("Path is " + path);
    
        try 
        {
            return new URL ("file", null, path);
        }
        catch (java.net.MalformedURLException e) 
        {
            // According to the spec this could only happen if the file
            // protocol were not recognized.
            throw new Exception ("unexpected MalformedURLException");
        }
    }
}
