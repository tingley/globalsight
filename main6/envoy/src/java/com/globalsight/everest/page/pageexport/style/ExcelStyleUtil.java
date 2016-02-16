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
import java.util.regex.Matcher;
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

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.page.pageexport.style.xlsx.AtStyleStyle;
import com.globalsight.everest.page.pageexport.style.xlsx.BoldStyle;
import com.globalsight.everest.page.pageexport.style.xlsx.ItalicStyle;
import com.globalsight.everest.page.pageexport.style.xlsx.Style;
import com.globalsight.everest.page.pageexport.style.xlsx.SubscriptStyle;
import com.globalsight.everest.page.pageexport.style.xlsx.SuperscriptStyle;
import com.globalsight.everest.page.pageexport.style.xlsx.UnderlineStyle;
import com.globalsight.ling.docproc.extractor.msoffice2010.ExcelExtractor;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

/**
 * A util class that used to handle the docx style tag.
 */
public class ExcelStyleUtil extends StyleUtil
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
            .getLogger(ExcelStyleUtil.class);

    private static Pattern PATTERN_SI_COUNT = Pattern
            .compile("(<sst[^>]*?count=\")([\\d]*?)(\"[^>]*?>)");

    // find the non-numeric characters in the sheet value
    private static Pattern PATTERN_SHEET_ROW = Pattern
            .compile("(<c [^>]*)(><v>)([^<]*\\D+[^<]*)(</v></c>)");

    private static Pattern PATTERN_SHARED_STRING_SI = Pattern
            .compile("(</si>)([\r\n]*?</sst>)");

    private static Pattern PATTERN_WORKBOOK_RSID = Pattern
            .compile("<Relationship Id=\"rId(\\d+)\"");

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
        File file = new File(path);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "utf-8"));

        Document document = null;
        try
        {
            document = db.parse(new InputSource(br));
        }
        catch (Exception e1)
        {
            try
            {
                br.close();
            }
            catch (Exception e2)
            {
                // ignore;
            }

            try
            {
                String content = FileUtil.readFile(file, "UTF-8");

                while (content.charAt(0) != '<')
                {
                    content = content.substring(1);
                }
                FileUtil.writeFile(file, content, "UTF-8");

                br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), "utf-8"));
                document = db.parse(new InputSource(br));
            }
            catch (Exception e2)
            {
                s_logger.error(e1);
            }
        }
        
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
            File file = new File(filePath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "utf-8"));
            Document document = null;
            try
            {
                document = db.parse(new InputSource(br));
            }
            catch (Exception e1)
            {
                try
                {
                    br.close();
                }
                catch (Exception e2)
                {
                    // ignore;
                }

                try
                {
                    String content = FileUtil.readFile(file, "UTF-8");

                    while (content.charAt(0) != '<')
                    {
                        content = content.substring(1);
                    }
                    FileUtil.writeFile(file, content, "UTF-8");

                    br = new BufferedReader(new InputStreamReader(
                            new FileInputStream(file), "utf-8"));
                    document = db.parse(new InputSource(br));
                }
                catch (Exception e2)
                {
                    s_logger.error(e1);
                }
            }
            
            
            String name = document.getFirstChild().getNodeName();
            if (ExcelExtractor.usePptxStyle(name))
            {
                PptxStyleUtil u = new PptxStyleUtil();
                u.updateBeforeExport(filePath);
            }
            else
            {
                recoverSheetRows(filePath);
                repairAttributeValue(filePath);
                forStylesInWt(filePath);
                forStylesNotInWt(filePath);
            }
        }
        catch (Exception e)
        {
            s_logger.error(e);
        }
    }

    /**
     * Recovers the row contents in sheet file so that the exported file can be
     * opened correctly.
     * <p>
     * For example, {@code <c r="A1"><v>aaa</v></c>} needs to be transformed to
     * <p>
     * {@code <c r="A1" t="s"><v>100</v></c>}. Value "aaa" is moved to the
     * sharedString.xml and number "100" is the corresponding position of "aaa"
     * in sharedString.xml.
     * 
     * @since GBS-2973
     */
    private void recoverSheetRows(String path) throws Exception
    {
        String fileName = FileUtils.getBaseName(path);
        if (!fileName.startsWith("sheet") || !fileName.endsWith(".xml"))
        {
            // only focus on sheetXX.xml
            return;
        }
        boolean needRecover = false;
        File sheetFile = new File(path);
        File sharedStringFile = new File(sheetFile.getParentFile().getParent(),
                "sharedStrings.xml");
        String sharedString = null;
        if (!sharedStringFile.exists())
        {
            sharedString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"0\" uniqueCount=\"0\"></si></sst>";
        }
        else
        {
            sharedString = FileUtil.readFile(sharedStringFile, "utf-8");
        }
        String siCount = getSiCount(sharedString);
        if (siCount == null)
        {
            return;
        }
        String sheet = FileUtil.readFile(sheetFile, "utf-8");

        int siNumberInSharedString = Integer.valueOf(siCount);
        Matcher sheetMatcher = PATTERN_SHEET_ROW.matcher(sheet);
        // <c r="A1"><v>aaa</v></c> -> <c r="A1"
        // t="s"><v>[siNumberInSharedString]</v></c>
        while (sheetMatcher.find())
        {
            String value = sheetMatcher.group(3);
            if (value.startsWith("-") && value.substring(1).matches("\\d+"))
            {
                // negative also needs to be filtered
                continue;
            }

            String group1 = sheetMatcher.group(1);
            group1 = group1.replaceAll(" t=\"[^\"]*\"", "");
            String newString = group1 + " t=\"s\"" + sheetMatcher.group(2)
                    + siNumberInSharedString + sheetMatcher.group(4);

            Matcher sharedStringMatcher = PATTERN_SHARED_STRING_SI
                    .matcher(sharedString);
            if (sharedStringMatcher.find())
            {
                needRecover = true;
                sheet = StringUtil.replace(sheet, sheetMatcher.group(),
                        newString);

                String newSi = "<si><t>" + value + "</t></si>";
                if (!sharedString.contains("<si>"))
                {
                    newString = newSi + sharedStringMatcher.group(2);
                }
                else
                {
                    newString = sharedStringMatcher.group(1) + newSi
                            + sharedStringMatcher.group(2);
                }
                sharedString = StringUtil.replace(sharedString,
                        sharedStringMatcher.group(), newString);
            }
            siNumberInSharedString++;
        }
        if (needRecover)
        {
            FileUtil.writeFile(sheetFile, sheet, "utf-8");
            sharedString = updateSiCount(sharedString, siNumberInSharedString);
            FileUtil.writeFile(sharedStringFile, sharedString, "utf-8");
            updateOtherNativeXmlFiles(sheetFile);
        }
    }

    /**
     * Updates other native xml files that need to add sharedString xml entry.
     */
    private void updateOtherNativeXmlFiles(File sheetFile) throws Exception
    {
        // 1. update xl\_rels\workbook.xml.rels, by adding
        // <Relationship Id="rIdXX"
        // Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings"
        // Target="sharedStrings.xml"/>
        File workbookXmlRelsFile = new File(sheetFile.getParentFile()
                .getParent(), "_rels/workbook.xml.rels");
        if (workbookXmlRelsFile.exists())
        {
            String workbookXmlRels = FileUtil.readFile(workbookXmlRelsFile,
                    "utf-8");
            if (!workbookXmlRels.contains("sharedStrings.xml"))
            {
                Matcher m = PATTERN_WORKBOOK_RSID.matcher(workbookXmlRels);
                List<Integer> rIds = new ArrayList<Integer>();
                while (m.find())
                {
                    rIds.add(Integer.parseInt(m.group(1)));
                }
                SortUtil.sort(rIds);
                int maxRId = rIds.get(rIds.size() - 1) + 1;
                String toAdd = "<Relationship Id=\"rId"
                        + maxRId
                        + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\" Target=\"sharedStrings.xml\"/>";
                workbookXmlRels = StringUtil.replace(workbookXmlRels,
                        "</Relationships>", toAdd + "</Relationships>");
                FileUtil.writeFile(workbookXmlRelsFile, workbookXmlRels,
                        "utf-8");
            }
        }
        // 2. update [Content_Types].xml, by adding
        // <Override PartName="/xl/sharedStrings.xml"
        // ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
        File contentTypesFile = new File(sheetFile.getParentFile()
                .getParentFile().getParent(), "[Content_Types].xml");
        if (contentTypesFile.exists())
        {
            String contentTypes = FileUtil.readFile(contentTypesFile, "utf-8");
            if (!contentTypes.contains("sharedStrings.xml"))
            {
                String toAdd = "<Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>";
                contentTypes = StringUtil.replace(contentTypes, "</Types>",
                        toAdd + "</Types>");
                FileUtil.writeFile(contentTypesFile, contentTypes, "utf-8");
            }
        }
    }

    /**
     * Updates the si count number in sharedString.xml.
     */
    private String updateSiCount(String sharedString, int newSiCount)
    {
        Matcher m = PATTERN_SI_COUNT.matcher(sharedString);
        if (m.find())
        {
            String newString = m.group(1) + newSiCount + m.group(3);
            sharedString = StringUtil.replace(sharedString, m.group(),
                    newString);
        }
        return sharedString;
    }

    /**
     * Finds the si count in sharedString.xml.
     */
    private String getSiCount(String sharedString)
    {
        Matcher m = PATTERN_SI_COUNT.matcher(sharedString);
        if (m.find())
        {
            return m.group(2);
        }
        return null;
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
