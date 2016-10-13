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
package com.globalsight.everest.page.pageexport.style;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xalan.transformer.TransformerIdentityImpl;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.globalsight.everest.page.pageexport.style.pptx.AtBrStyle;
import com.globalsight.everest.page.pageexport.style.pptx.AtStyleStyle;
import com.globalsight.everest.page.pageexport.style.pptx.BoldStyle;
import com.globalsight.everest.page.pageexport.style.pptx.ItalicStyle;
import com.globalsight.everest.page.pageexport.style.pptx.Style;
import com.globalsight.everest.page.pageexport.style.pptx.SubscriptStyle;
import com.globalsight.everest.page.pageexport.style.pptx.SuperscriptStyle;
import com.globalsight.everest.page.pageexport.style.pptx.UnderlineStyle;
import com.globalsight.util.FileUtil;

/**
 * A util class that used to handle the docx style tag.
 */
public class PptxStyleUtil extends StyleUtil
{
    /**
     * The map is used to replace the style tag.
     * <p>
     * Before adding removed tag, all style tags should be replaced by random
     * strings, and the random strings and replaced string will be put to the
     * <code>style</code> map. In this way, the style tag will be treated as
     * normal text.
     * <p>
     * After adding removed tag, all replaced string will be put back.
     */
    private Map<String, String> styles = new HashMap<String, String>();

    static private final Logger s_logger = Logger
            .getLogger(PptxStyleUtil.class);

    /**
     * @see com.globalsight.everest.page.pageexport.style.StyleUtil#preHandle(java.lang.String)
     */
    @Override
    public String preHandle(String content)
    {
        OfficeTagUtil util = new OfficeTagUtil();
        content = util.handleString(content);

        List<String> bs = new ArrayList<String>();

        for (Pattern p : OFFICE_PATTERNS)
        {
            bs.addAll(getAllString(p, content));
        }

        for (String b : bs)
        {
            String r = getRandom();
            content = content.replace(b, r);
            styles.put(r, b);
        }

        return content;
    }

    /**
     * @see com.globalsight.everest.page.pageexport.style.StyleUtil#sufHandle(java.lang.String)
     */
    @Override
    public String sufHandle(String content)
    {
        for (String r : styles.keySet())
        {
            content = content.replace(r, styles.get(r));
        }

        return content;
    }

    /**
     * Handles all style tags in w:t nodes.
     * <p>
     * For example:
     * 
     * <pre>
     * &lt;w:r w:rsidRPr="00AF0D5E"&gt;
     *     &lt;w:rPr&gt;
     *         &lt;w:rStyle w:val="DONOTTRANSLATEchar"&gt;&lt;/w:rStyle&gt;
     *     &lt;/w:rPr&gt;
     *     &lt;w:t xml:space="preserve"&gt;this &lt;b&gt;is&lt;/b&gt; a test &lt;/w:t&gt;
     * &lt;/w:r&gt;
     * </pre>
     * 
     * should be changed to
     * 
     * <pre>
     * &lt;w:r w:rsidRPr="00AF0D5E"&gt;
     *     &lt;w:rPr&gt;
     *         &lt;w:rStyle w:val="DONOTTRANSLATEchar"&gt;&lt;/w:rStyle&gt;
     *     &lt;/w:rPr&gt;
     *     &lt;w:t xml:space="preserve"&gt;this &lt;/w:t&gt;
     * &lt;/w:r&gt;
     * &lt;w:r w:rsidRPr="00AF0D5E"&gt;
     *     &lt;w:rPr&gt;
     *         &lt;w:rStyle w:val="DONOTTRANSLATEchar"&gt;&lt;/w:rStyle&gt;
     *         &lt;w:b/&gt;
     *     &lt;/w:rPr&gt;
     *     &lt;w:t xml:space="preserve"&gt;is&lt;/w:t&gt;
     * &lt;/w:r&gt;
     *     &lt;w:r w:rsidRPr="00AF0D5E"&gt;
     *     &lt;w:rPr&gt;
     *         &lt;w:rStyle w:val="DONOTTRANSLATEchar"&gt;&lt;/w:rStyle&gt;
     *     &lt;/w:rPr&gt;
     *     &lt;w:t xml:space="preserve"&gt;a test &lt;/w:t&gt;
     * &lt;/w:r&gt;
     * </pre>
     * 
     * @param path
     *            the file path
     * @throws Exception
     */
    private void forStylesInWt(String path) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(path)), "utf-8"));

        Document document = db.parse(new InputSource(br));
        List<Style> styles = getAllStyles();

        // For style node can be nested. Adding styles should be execute several
        // times. It will be stopped only if there is nothing changed during one
        // executing.
        int oldNumber = Integer.MAX_VALUE;
        int newNumber = 0;
        while (true)
        {
            newNumber = 0;
            for (Style style : styles)
            {
                newNumber += style.addStyles(document);
            }

            // Break if nothing changed.
            if (newNumber == 0 || newNumber == oldNumber)
            {
                break;
            }

            oldNumber = newNumber;
        }

        saveToFile(document, path);
    }

    /**
     * Gets all styles.
     * 
     * @return
     */
    private List<Style> getAllStyles()
    {
        List<Style> styles = new ArrayList<Style>();
        styles.add(new BoldStyle());
        styles.add(new ItalicStyle());
        styles.add(new UnderlineStyle());
        styles.add(new SuperscriptStyle());
        styles.add(new SubscriptStyle());
        styles.add(new AtStyleStyle());
        styles.add(new AtBrStyle());
        return styles;
    }

    /**
     * @see com.globalsight.everest.page.pageexport.style.StyleUtil#updateBeforeExport(java.lang.String)
     */
    @Override
    public void updateBeforeExport(String filePath)
    {
        try
        {
            repairAttributeValue(filePath);
            forStylesInWt(filePath);
            forStylesNotInWt(filePath);
        }
        catch (Exception e)
        {
            s_logger.error(e);
        }
    }

    private void forStylesNotInWt(String filePath)
    {
        try
        {
            String content = FileUtil.readFile(new File(filePath), "utf-8");
            content = content.replaceAll("<[/]?[biu]?>", "");
            content = content.replaceAll("<[/]?su[bp]?>", "");

            // For text not in node.
            content = content.replaceAll("</>\\s*[^\\t\\n\\x0B\\f\\r<]\\s*<",
                    "");
            FileUtil.writeFile(new File(filePath), content, "utf-8");
        }
        catch (IOException e)
        {
            s_logger.error(e);
        }
    }

    /**
     * Saves the document to a XML files.
     * 
     * @param document
     *            the document to save.
     * @param path
     *            the file path.
     * @throws Exception
     */
    private void saveToFile(Document document, String path) throws Exception
    {
        TransformerIdentityImpl transformer = new TransformerIdentityImpl();
        DOMSource source = new DOMSource(document);
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        OutputStreamWriter ou = new OutputStreamWriter(new FileOutputStream(
                path), "UTF-8");
        StreamResult result = new StreamResult(ou);
        transformer.transform(source, result);
    }
}
