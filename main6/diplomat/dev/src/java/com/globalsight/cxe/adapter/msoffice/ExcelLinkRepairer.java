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

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.ling.common.XmlEntities;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

public class ExcelLinkRepairer
{
    private String sourceRoot;

    private String targetRoot;

    private XmlEntities m_xmlEntityConverter = new XmlEntities();

    private static final String WORK_BOOK = "/xl/workbook.xml";
    private static final String ROOT_SHEETS = "/xl/worksheets";
    private static final String ROOT_DRAWINGS_RELS = "/xl/drawings/_rels";
    private static Pattern SHEET_NAME = Pattern.compile("<sheet name=\"([^\"]{32,})\" ");
    private static Pattern C_PATTERN = Pattern
            .compile("(<c [^>]*><f>')([^']*)('[^<]*</f><v>[^<]*</v></c>)");
    private static Pattern C2_PATTERN = Pattern
            .compile("(<c [^>]*><f>)&apos;([^<]*)&apos;([^<]*</f><v>[^<]*</v></c>)");
    private static Pattern HYPERLINK = Pattern
            .compile("(<hyperlink ref=\"[^\"]*\" location=\")([^!\"]*)(![^\"]*\" [^/]*/>)");
    private static Pattern RELATION_SHIP = Pattern
            .compile("(<Relationship Id=\"[^\"]*\" Type=\"[^\"]*\" Target=\")([^!\"]*)(![^\"]*\"[^/]*/>)");

    public String getSourceRoot()
    {
        return sourceRoot;
    }

    public void setSourceRoot(String sourceRoot)
    {
        this.sourceRoot = sourceRoot;
    }

    public String getTargetRoot()
    {
        return targetRoot;
    }

    public void setTargetRoot(String targetRoot)
    {
        this.targetRoot = targetRoot;
    }
    
    private void repairWorkBook(File workbook) throws Exception
    {
        String content = FileUtil.readFile(workbook, "utf-8");
        Matcher m = SHEET_NAME.matcher(content);

        int index = 0;
        StringBuilder sb = new StringBuilder();
        
        while (m.find())
        {
            // sheet name
            String s = m.group(1);
            //remove the style in name. removed style:  b i u sub sup
            s = StringUtil.replaceWithRE(s, "<[/]{0,1}[bius][u]{0,1}[bp]{0,1}>", "");
            
            String sheetname = m_xmlEntityConverter.decodeStringBasic(s);
            if (sheetname.length() > 31)
            {
                // sheet names that exceed 31 characters need to be cut shorter
                // because excel can not show that full name
                String ss = sheetname.substring(0, 31);
                ss = m_xmlEntityConverter.encodeStringBasic(ss);
                ss = StringUtil.replace(ss, "/", "");
                
                sb.append(content.substring(index, m.start()));
                sb.append("<sheet name=\"");
                sb.append(ss);
                sb.append("\" ");
                
                index = m.end();
            }
        }
        
        sb.append(content.substring(index));

        FileUtil.writeFile(workbook, sb.toString(), "utf-8");
    }

    public void repair() throws Exception
    {
        if (sourceRoot == null || targetRoot == null)
            return;

        String sPath = sourceRoot + ".1" + WORK_BOOK;
        File sWorkBook = new File(sPath);
        if (!sWorkBook.exists())
            return;

        String tPath = targetRoot + ".1" + WORK_BOOK;
        File tWorkBook = new File(tPath);
        if (!tWorkBook.exists())
            return;

        repairWorkBook(tWorkBook);

        Map<String, String> oldNames = getAllSheetName(sWorkBook);
        Map<String, String> newNames = getAllSheetName(tWorkBook);

        File drawingsRelsDir = new File(targetRoot + ".1" + ROOT_DRAWINGS_RELS);
        if (drawingsRelsDir.exists())
        {
            List<File> drawingsRels = FileUtil.getAllFiles(drawingsRelsDir,
                    new FileFilter()
                    {
                        @Override
                        public boolean accept(File pathname)
                        {
                            return pathname.getName().endsWith(".xml.rels");
                        }
                    });

            for (File f : drawingsRels)
            {
                repairLinkForDrawings(f, oldNames, newNames);
            }
        }

        List<File> sheets = FileUtil.getAllFiles(new File(targetRoot + ".1"
                + ROOT_SHEETS), new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getName().endsWith(".xml");
            }
        });

        for (File f : sheets)
        {
            repairLinkForTexts(f, oldNames, newNames);
            repairFormulas(f, oldNames, newNames);
        }
    }

    private Map<String, String> getAllSheetName(File workbook) throws Exception
    {
        HashMap<String, String> sheetNames = new HashMap<String, String>();

        String content = FileUtil.readFile(workbook, "utf-8");
        Pattern p = Pattern
                .compile("<sheet name=\"([^\"]*)\" sheetId=\"([^\"]*)\" r:id=\"([^\"]*)\"/>");
        Matcher m = p.matcher(content);

        while (m.find())
        {
            sheetNames.put(m.group(2), m.group(1));
        }

        return sheetNames;
    }

    private void repairFormulas(File f, Map<String, String> oldNames,
            Map<String, String> newNames) throws Exception
    {
        String content = FileUtil.readFile(f, "utf-8");
        Matcher m = C_PATTERN.matcher(content);
        
        int index = 0;
        StringBuilder sb = new StringBuilder();
        
        while (m.find())
        {
            String name = m.group(2);

            if (oldNames.containsValue(name))
            {
                String key = null;

                for (String k : oldNames.keySet())
                {
                    if (oldNames.get(k).equals(name))
                    {
                        key = k;
                        break;
                    }
                }

                String newLink = m.group(1) + newNames.get(key) + m.group(3);

                sb.append(content.substring(index, m.start()));
                sb.append(newLink);                
                index = m.end();
            }
        }
        
        sb.append(content.substring(index));
        content = sb.toString();
        
        index = 0;
        sb = new StringBuilder();

        m = C2_PATTERN.matcher(content);
        while (m.find())
        {
            String name = m.group(2);

            if (oldNames.containsValue(name))
            {
                String key = null;

                for (String k : oldNames.keySet())
                {
                    if (oldNames.get(k).equals(name))
                    {
                        key = k;
                        break;
                    }
                }

                String newLink = m.group(1) + "'" + newNames.get(key) + "'"
                        + m.group(3);

                sb.append(content.substring(index, m.start()));
                sb.append(newLink);                
                index = m.end();
            }
        }
        
        sb.append(content.substring(index));

        FileUtil.writeFile(f, sb.toString(), "utf-8");
    }

    private void repairLinkForTexts(File f, Map<String, String> oldNames,
            Map<String, String> newNames) throws Exception
    {
        String content = FileUtil.readFile(f, "utf-8");

        Matcher m = HYPERLINK.matcher(content);
        int index = 0;
        StringBuilder sb = new StringBuilder();
        
        while (m.find())
        {
            String name = m.group(2);

            boolean removedApos = false;
            if (name.startsWith("'") && name.endsWith("'"))
            {
                removedApos = true;
                name = name.substring(1, name.length() - 1);
            }

            if (oldNames.containsValue(name))
            {
                String key = null;

                for (String k : oldNames.keySet())
                {
                    if (oldNames.get(k).equals(name))
                    {
                        key = k;
                        break;
                    }
                }

                String value = StringUtil.replace(newNames.get(key), "&apos;", "''");
                if (removedApos)
                {
                    value = "'" + value + "'";
                }

                String newLink = m.group(1) + value + m.group(3);

      		    sb.append(content.substring(index, m.start()));
                sb.append(newLink);                
                index = m.end();
            }
        }

		sb.append(content.substring(index));
		
        FileUtil.writeFile(f, sb.toString(), "utf-8");
    }

    private void repairLinkForDrawings(File f, Map<String, String> oldNames,
            Map<String, String> newNames) throws Exception
    {
        String content = FileUtil.readFile(f, "utf-8");

        Matcher m = RELATION_SHIP.matcher(content);
        int index = 0;
        StringBuilder sb = new StringBuilder();
        
        while (m.find())
        {
            String name = m.group(2);

            boolean removedApos = false;
            if (name.startsWith("#'") && name.endsWith("'"))
            {
                removedApos = true;
                name = name.substring(2, name.length() - 1);
            }

            if (oldNames.containsValue(name))
            {
                String key = null;

                for (String k : oldNames.keySet())
                {
                    if (oldNames.get(k).equals(name))
                    {
                        key = k;
                        break;
                    }
                }

                String value = StringUtil.replace(newNames.get(key),"&apos;", "''");
                if (removedApos)
                {
                    value = "#'" + value + "'";
                }

                String newLink = m.group(1) + value + m.group(3);
      		    sb.append(content.substring(index, m.start()));
                sb.append(newLink);                
                index = m.end();
            }
        }

        sb.append(content.substring(index));
        FileUtil.writeFile(f, sb.toString(), "utf-8");
    }
}
