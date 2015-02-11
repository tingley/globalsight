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
package test;

import com.globalsight.ling.common.DiplomatBasicHandler;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;


/**
 * Visual Extraction Validator.  This class produces an HTML file that
 * color codes the extracted segment in the original file.
 * Translatable content - red
 * Localizable content - yellow
 */
public class VisualExtractionValidator
    implements DiplomatBasicHandler
{   
    private final static String TRANSLATABLE = "translatable";
    private final static String LOCALIZABLE = "localizable";
    
    //    private XmlEntities xmlDecoder = new XmlEntities();
    private StringBuffer htmlText;


    public static void main(String[] args)
        throws FileNotFoundException, UnsupportedEncodingException,
               IOException, DiplomatBasicParserException
    {
        if(args.length < 2)
        {
            System.err.println("USAGE: java VisualExtractionValidator GXML_file output_file");
            System.exit(1);
        }

        InputStream in = new FileInputStream(args[0]);
        BufferedReader reader
            = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuffer gxml = new StringBuffer();
        String line;
        // Discard XML decralation (DiplomatBasicParser chokes on it)
        reader.readLine();
        while((line = reader.readLine()) != null)
        {
            gxml.append(line);
            gxml.append("\n");
        }
        reader.close();
        
        VisualExtractionValidator handler = new VisualExtractionValidator();
        DiplomatBasicParser parser = new DiplomatBasicParser(handler);
        parser.parse(gxml.toString());

        OutputStream out = new FileOutputStream(args[1]);
        BufferedWriter writer
            = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        String result = handler.getResult();
        writer.write(result, 0, result.length());
        writer.close();
    }
    
        
    /**
     * Returns the HTML string ready to display in a browser
     */
    public String getResult()
    {
        return 	htmlText.toString();
    }

    /**
     * Handles diplomat basic parser Start event.
     */
    public void handleStart()
        throws DiplomatBasicParserException
    {
        htmlText = new StringBuffer();
        htmlText.append("<HTML>\n<HEAD>\n<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n</HEAD>\n<BODY>\n<PRE>\n");
    }

    /**
     * Handles diplomat basic parser Stop event.
     */
    public void handleStop()
        throws DiplomatBasicParserException
    {
        htmlText.append("\n</PRE>\n</BODY>\n</HTML>\n");
    }

    /**
    * End-tag event handler.
    * @param p_strName - the literal tag name
    * @param p_strOriginalTag - the complete raw token from the parser
    */
    public void handleEndTag(String p_strName, String p_strOriginalTag)
    {
        if(p_strName.equals(LOCALIZABLE) || p_strName.equals(TRANSLATABLE))
        {
            htmlText.append("</FONT>");
        }
    }

    /**
    * Start-tag event handler.
    * @param p_strTmxTagName - The literal tag name.
    * @param p_hAtributes - Tag attributes in the form of a hashtable.
    * @param p_strOriginalString - The complete raw token from the parser.
    */
    public void handleStartTag(String p_strTmxTagName,
                               Properties p_hAttributes,
                               String p_strOriginalString)
        throws DiplomatBasicParserException
    {
        if(p_strTmxTagName.equals(TRANSLATABLE))
        {
            htmlText.append("<FONT COLOR=red>");
        }
        else if(p_strTmxTagName.equals(LOCALIZABLE))
        {
            htmlText.append("<FONT COLOR=yellow>");
        }
    }

    /**
    * Text event handler.
    * @param p_strText - the next text chunk from between the tags
    */
    public void handleText(String p_strText)
        throws DiplomatBasicParserException
    {
        if(p_strText.trim().length() != 0)
        {
            htmlText.append(replaceApos(p_strText));
        }
    }

    /*
     * replace &apos; with '
     */
    private String replaceApos(String original)
    {
        StringBuffer buf = new StringBuffer();
        int prevLast = 0;
        int index = prevLast;
        String apos = "&apos;";
        int lenApos = apos.length();
        
        while((index = original.indexOf("&apos;", index)) != -1)
        {
            buf.append(original.substring(prevLast, index));
            buf.append('\'');
            index += lenApos;
            prevLast = index;
        }
        buf.append(original.substring(prevLast, original.length()));
        return buf.toString();
    }
    
}
