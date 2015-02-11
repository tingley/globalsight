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
package com.globalsight.cxe.adapter.msoffice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.ling.docproc.extractor.xml.XPathAPIJdk;

public class ExcelHiddenHandler
{
    static private final Logger s_logger = Logger
            .getLogger(ExcelHiddenHandler.class);

    private String dir;
    private static final String XLSX_SHEET_NAME = "xl/workbook.xml";
    private static final String XLSX_SHEETS_DIR = "xl/worksheets";

    // return
    private HashMap<String, String> hideCellMap = new HashMap<String, String>();
    private Set<String> hiddenSharedString = new HashSet<String>();
    private Set<String> hiddenSheetIds = new HashSet<String>();

    // not return
    private Set<Integer> hiddenCols = new HashSet<Integer>();
    private String sheetsDir = null;
    private String sheetnameXml = null;
    private List<String> hideCellStyleIds = null;

    /**
     * @param sheet
     */
    public ExcelHiddenHandler(String dir, List<String> hideCellStyleIds)
    {
        super();
        this.dir = dir;
        this.hideCellStyleIds = hideCellStyleIds;

        this.sheetsDir = FileUtils.concatPath(dir, XLSX_SHEETS_DIR);
        this.sheetnameXml = FileUtils.concatPath(dir, XLSX_SHEET_NAME);
    }

    private Document getDocument(File f) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), "utf-8"));
        Document document = db.parse(new InputSource(br));

        return document;
    }

    private void getExcelHiddenSheetId()
    {
        try
        {
            Document document = getDocument(new File(sheetnameXml));

            String xpath = "//*[local-name()=\"sheet\"][@state=\"hidden\"]";
            NodeList affectedNodes = XPathAPIJdk
                    .selectNodeList(document, xpath);

            if (affectedNodes != null && affectedNodes.getLength() > 0)
            {
                int len = affectedNodes.getLength();
                for (int i = 0; i < len; i++)
                {
                    Element nd = (Element) affectedNodes.item(i);
                    String id = nd.getAttribute("sheetId");
                    String rid = nd.getAttribute("r:id");
                    if (rid != null && rid.startsWith("rId"))
                    {
                        id = rid.substring(3);
                    }

                    hiddenSheetIds.add(id);
                }
            }

        }
        catch (Exception e)
        {
            s_logger.error(e);
        }
    }

    private void getSharedIdInSheet(String sheet)
    {
        try
        {
            String xpath = "//*[local-name()=\"c\"][@t=\"s\"]/*[local-name()=\"v\"]";

            Document document = getDocument(new File(sheet));
            NodeList affectedNodes = XPathAPIJdk
                    .selectNodeList(document, xpath);

            if (affectedNodes != null && affectedNodes.getLength() > 0)
            {
                int len = affectedNodes.getLength();
                for (int i = 0; i < len; i++)
                {
                    Element nd = (Element) affectedNodes.item(i);
                    String id = nd.getFirstChild().getNodeValue();
                    hiddenSharedString.add(id);
                }
            }
        }
        catch (Exception e)
        {
            s_logger.error(e);
        }
    }

    private void handleHiddenSheets()
    {
        getExcelHiddenSheetId();
        for (String id : hiddenSheetIds)
        {
            String path = FileUtils
                    .concatPath(sheetsDir, "sheet" + id + ".xml");
            getSharedIdInSheet(path);
        }
    }

    private File[] getVisibleSheets()
    {
        File[] files = new File(sheetsDir).listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                if (!pathname.isFile())
                {
                    return false;
                }

                String basename = FileUtils.getBaseName(pathname.getPath());
                if (!basename.startsWith("sheet"))
                {
                    return false;
                }

                String fprefix = FileUtils.getPrefix(basename);
                String fid = fprefix.substring(5);
                if (hiddenSheetIds.contains(fid))
                {
                    return false;
                }

                return true;
            }
        });

        return files;
    }

    private Set<String> getHiddenRowIds(Document document) throws Exception
    {
        Set<String> hideRowIds = new HashSet<String>();

        String xpath = "//*[local-name()=\"row\"][@hidden=\"1\"]";
        NodeList affectedNodes = XPathAPIJdk.selectNodeList(document, xpath);

        if (affectedNodes != null && affectedNodes.getLength() > 0)
        {
            int len = affectedNodes.getLength();
            for (int i = 0; i < len; i++)
            {
                Element nd = (Element) affectedNodes.item(i);
                String id = nd.getAttribute("r");
                hideRowIds.add(id);
            }
        }

        return hideRowIds;
    }

    private Set<String> getHiddenColIds(Document document) throws Exception
    {
        Set<String> hideColIds = new HashSet<String>();

        String xpath = "//*[local-name()=\"cols\"]/*[local-name()=\"col\"][@hidden=\"1\"] "
                + "| //*[local-name()=\"cols\"]/*[local-name()=\"col\"][@hidden=\"true\"]";

        NodeList affectedNodes = XPathAPIJdk.selectNodeList(document, xpath);

        if (affectedNodes != null && affectedNodes.getLength() > 0)
        {
            int len = affectedNodes.getLength();
            for (int i = 0; i < len; i++)
            {
                Element nd = (Element) affectedNodes.item(i);
                String min = nd.getAttribute("min");
                String max = nd.getAttribute("max");

                int m1 = Integer.parseInt(min);
                int m2 = Integer.parseInt(max);

                for (int j = m1; j <= m2; j++)
                {
                    hideColIds.add(getExcelColumnChar(j));
                }
            }
        }

        return hideColIds;
    }

    private String getExcelColumnChar(int num)
    {
        int A = 65;
        String sCol = "";
        int iRemain = 0;

        if (num > 701)
        {
            return "";
        }

        if (num <= 26)
        {
            if (num == 0)
            {
                sCol = "" + (char) ((A + 26) - 1);
            }
            else
            {
                sCol = "" + (char) ((A + num) - 1);
            }
        }
        else
        {
            iRemain = (num / 26) - 1;
            if ((num % 26) == 0)
            {
                sCol = getExcelColumnChar(iRemain) + getExcelColumnChar(0);
            }
            else
            {
                sCol = (char) (A + iRemain) + getExcelColumnChar(num % 26);
            }
        }

        return sCol;
    }

    private boolean isHiddenCel(Element c, Set<String> hiddenRows,
            Set<String> hiddenCols)
    {
        String style = c.getAttribute("s");
        if (hideCellStyleIds.contains(style))
        {
            return true;
        }

        Element row = (Element) c.getParentNode();
        String r = row.getAttribute("r");
        if (hiddenRows.contains(r))
            return true;

        String p = c.getAttribute("r");
        String col = p.substring(0, p.length() - r.length());
        if (hiddenCols.contains(col))
            return true;

        return false;
    }

    private String setToString(Set<String> set)
    {
        if (set.size() == 0)
            return "";

        StringBuilder s = new StringBuilder();
        for (String style : set)
        {
            style = style.trim();
            if (style.length() > 0)
            {
                if (s.length() > 0)
                {
                    s.append(",");
                }
                s.append(style);
            }
        }

        return s.toString();
    }

    private boolean isSharedString(Element c)
    {
        String t = c.getAttribute("t");
        return "s".equals(t);
    }

    private void handleVisibleSheets()
    {
        File[] fs = getVisibleSheets();
        for (File f : fs)
        {
            if (hasHiddenContent(f))
            {
                Set<String> hiddenCells = new HashSet<String>();

                try
                {
                    Document document = getDocument(f);
                    Set<String> hiddenRows = getHiddenRowIds(document);
                    Set<String> hiddenCols = getHiddenColIds(document);

                    String xpath = "//*[local-name()=\"row\"]"
                            + "/*[local-name()=\"c\"]/*[local-name()=\"v\"]";

                    NodeList affectedNodes = XPathAPIJdk.selectNodeList(
                            document, xpath);

                    if (affectedNodes != null && affectedNodes.getLength() > 0)
                    {
                        int len = affectedNodes.getLength();
                        for (int i = 0; i < len; i++)
                        {
                            Element v = (Element) affectedNodes.item(i);
                            Element c = (Element) v.getParentNode();

                            if (isHiddenCel(c, hiddenRows, hiddenCols))
                            {
                                if (isSharedString(c))
                                {
                                    String vId = v.getTextContent();
                                    hiddenSharedString.add(vId);
                                }
                                hiddenCells.add(c.getAttribute("r"));
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    s_logger.error(e);
                }

                if (!hiddenCells.isEmpty())
                {
                    String sheetPath = f.getPath();
                    String fbasename = FileUtils.getBaseName(sheetPath);
                    String fprefix = FileUtils.getPrefix(fbasename);
                    String fid = fprefix.substring(5);

                    String hiddenCellIds = setToString(hiddenCells);
                    hideCellMap.put(fprefix, hiddenCellIds);
                    hideCellMap.put("comments" + fid, hiddenCellIds);
                }
            }
        }

    }

    private boolean hasHiddenContent(File f)
    {
        String text = "";
        try
        {
            text = FileUtils.read(f, "UTF-8");
        }
        catch (IOException e)
        {
            s_logger.error(e);
        }
        if (!text.contains(" hidden=\"1\"")
                && !text.contains("hidden=\"true\"")
                && (hideCellStyleIds == null || hideCellStyleIds.isEmpty()))
        {
            return false;
        }

        if (!text.contains("</row>"))
        {
            return false;
        }

        return true;
    }

    public void run()
    {
        handleHiddenSheets();
        handleVisibleSheets();
    }

    /**
     * @return the hideCellMap
     */
    public HashMap<String, String> getHideCellMap()
    {
        return hideCellMap;
    }

    /**
     * @return the hiddenSharedString
     */
    public String getHiddenSharedString()
    {
        return setToString(hiddenSharedString);
    }

    public Set<String> getHiddenSheetIds()
    {
        return hiddenSheetIds;
    }

    public static boolean isCommentFromHiddenSheet(Set<String> hiddenSheetIds,
            File commentFile)
    {
        if (hiddenSheetIds == null || hiddenSheetIds.size() == 0
                || commentFile == null)
        {
            return false;
        }

        String prefix = FileUtils.getPrefix(FileUtils.getBaseName(commentFile
                .getPath()));
        // comments1
        String id = prefix.substring(8);
        return hiddenSheetIds.contains(id);
    }
}
