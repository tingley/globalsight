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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

public class ExcelTableRepairer
{
    private String sourceRoot;

    private String targetRoot;

    private static final String SHARED_STRINGS = "/xl/sharedStrings.xml";
    private static final String ROOT_TABLES = "/xl/tables";
    private static final String ROOT_SHEETS = "/xl/worksheets";

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

    public void repair() throws Exception
    {
        if (sourceRoot == null || targetRoot == null)
            return;

        String sPath = sourceRoot + "." + OfficeXmlHelper.OFFICE_XLSX
                + SHARED_STRINGS;
        File sWorkBook = new File(sPath);
        if (!sWorkBook.exists())
            return;

        String tPath = targetRoot + "." + OfficeXmlHelper.OFFICE_XLSX
                + SHARED_STRINGS;
        File tWorkBook = new File(tPath);
        if (!tWorkBook.exists())
            return;

        List<String> oldNames = getAllSharedString(sWorkBook);
        List<String> newNames = getAllSharedString(tWorkBook);

        if (oldNames.size() != newNames.size())
            return;

        File tablesDir = new File(targetRoot + "."
                + OfficeXmlHelper.OFFICE_XLSX + ROOT_TABLES);
        if (tablesDir.exists())
        {
            List<File> tables = FileUtil.getAllFiles(tablesDir,
                    new FileFilter()
                    {
                        @Override
                        public boolean accept(File pathname)
                        {
                            String name = pathname.getName();
                            return name.startsWith("table")
                                    && name.endsWith("xml");
                        }
                    });

            for (File f : tables)
            {
                repairTables(f, oldNames, newNames);
            }
        }

        List<File> sheets = FileUtil.getAllFiles(new File(targetRoot + "."
                + OfficeXmlHelper.OFFICE_XLSX + ROOT_SHEETS), new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getName().endsWith(".xml");
            }
        });

        for (File f : sheets)
        {
            repairTablesInSheetXml(f, oldNames, newNames);
            // repairNumberWithComma(f);
        }
    }

    /**
     * In some cases, MT translates numbers from like 72.5 to 72,5, which
     * results in opening error to excel, therefore, we need to convert "," back
     * to ".".
     * <p>
     * for example, <v>72,5</v> needs to be changed to <v>72.5</v>.
     */
    private void repairNumberWithComma(File f) throws Exception
    {
        String content = FileUtil.readFile(f, "utf-8");
        Pattern p = Pattern.compile("(<v>)([\\d,]*)(</v>)");
        Matcher m = p.matcher(content);
        StringBuilder output = new StringBuilder();
        int start = 0;
        
        while (m.find(start))
        {
            String number = m.group(2);
            String newNumber = number.replace(",", ".");

            output.append(content.substring(start, m.start()));
            output.append(m.group(1));
            output.append(newNumber);
            output.append(m.group(3));
            start = m.end();
        }
        output.append(content.substring(start));

        FileUtil.writeFile(f, output.toString(), "utf-8");
    }

    private void repairTablesInSheetXml(File f, List<String> oldNames,
            List<String> newNames) throws Exception
    {
        String content = FileUtil.readFile(f, "utf-8");

        Pattern p = Pattern.compile("(<f>Table)([^<]*?)(</f>)");
        Matcher m = p.matcher(content);
        StringBuilder output = new StringBuilder();
        int start = 0;
        
        while (m.find(start))
        {
            String s = m.group(2);
            Pattern p2 = Pattern.compile("\\[[^\\[#][^\\]]*?]");
            Matcher m2 = p2.matcher(s);
            while (m2.find())
            {
                String name = m2.group();
                // remove []
                name = name.substring(1, name.length() - 1);
                // remove ' if has
                if (name.startsWith("'"))
                {
                    name = name.substring(1);
                }

                String newName = null;
                for (int i = 0; i < oldNames.size(); i++)
                {
                    if (name.equals(oldNames.get(i)))
                    {
                        newName = newNames.get(i);
                        break;
                    }
                }

                if (newName != null && !newName.equals(name))
                {
                    s = s.replace(m2.group(), "[" + newName + "]");
                }
            }

            output.append(content.substring(start, m.start()));
            output.append(m.group(1));
            output.append(s);
            output.append(m.group(3));
            start = m.end();
        }
        
        output.append(content.substring(start));

        FileUtil.writeFile(f, output.toString(), "utf-8");
    }

    private List<String> getAllSharedString(File workbook) throws Exception
    {
        List<String> sharedStrings = new ArrayList<String>();

        String content = FileUtil.readFile(workbook, "utf-8");
        Pattern p = Pattern.compile("<t[^>]*>([^<]*?)</t>");
        Matcher m = p.matcher(content);

        while (m.find())
        {
            String value = m.group(1);
            if (!StringUtil.isEmpty(value))
            {
                sharedStrings.add(value);
            }
        }

        return sharedStrings;
    }

    private void repairTables(File f, List<String> oldNames,
            List<String> newNames) throws Exception
    {
        String content = FileUtil.readFile(f, "utf-8");

        Pattern p = Pattern.compile("(<tableColumn [^>]* name=\")([^\"]*)\"");
        Matcher m = p.matcher(content);
        StringBuilder output = new StringBuilder();
        int start = 0;
        while (m.find(start))
        {
            String name = m.group(2);
            String newName = null;
            for (int i = 0; i < oldNames.size(); i++)
            {
                if (name.equals(oldNames.get(i)))
                {
                    newName = newNames.get(i);
                    break;
                }
            }

            if (newName != null && !newName.equals(name))
            {
                output.append(content.substring(start, m.start()));
                output.append(m.group(1));
                output.append(newName);
                output.append("\"");
                start = m.end();
            }
            else
            {
                output.append(content.substring(start, m.end()));
                start = m.end();
            }
        }
        output.append(content.substring(start));
        content = output.toString();
        
        output = new StringBuilder();
        start = 0;
        p = Pattern
                .compile("(<calculatedColumnFormula>)([^<]*?)(</calculatedColumnFormula>)");
        m = p.matcher(content);
        while (m.find(start))
        {
            String s = m.group(2);
            Pattern p2 = Pattern.compile("\\[[^\\[#][^\\]]*?]");
            Matcher m2 = p2.matcher(s);
            while (m2.find())
            {
                String name = m2.group();
                // remove []
                name = name.substring(1, name.length() - 1);
                // remove ' if has
                if (name.startsWith("'"))
                {
                    name = name.substring(1);
                }

                String newName = null;
                for (int i = 0; i < oldNames.size(); i++)
                {
                    if (name.equals(oldNames.get(i)))
                    {
                        newName = newNames.get(i);
                        break;
                    }
                }

                if (newName != null && !newName.equals(name))
                {
                    s = s.replace(m2.group(), "[" + newName + "]");
                }
            }
            
            output.append(content.substring(start, m.start()));
            output.append(m.group(1));
            output.append(s);
            output.append(m.group(3));
            start = m.end();
        }
        output.append(content.substring(start));

        FileUtil.writeFile(f, output.toString(), "utf-8");
    }
}
