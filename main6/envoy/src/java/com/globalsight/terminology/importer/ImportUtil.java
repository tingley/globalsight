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

package com.globalsight.terminology.importer;

//import com.globalsight.importer.ImportOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;

import org.apache.log4j.Logger;

import com.globalsight.terminology.Definition.Language;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.importer.ImportOptions.ColumnDescriptor;
import com.sun.org.apache.regexp.internal.RE;

/**
 * Reads CSV files and produces Entry objects.
 */
public class ImportUtil
{
    private static final Logger CATEGORY = Logger.getLogger(ImportUtil.class);

    //
    // Private Member Variables
    //

    private static String[] columnTypes =
    { "Skip this column", "Concept Status", "Concept Domain",
            "Concept Definition", "Concept Source", "Term Status",
            "Term Usage", "Term Type", "Term Definition", "Term Example",
            "Part of Speech", "Gender", "Number", "Source" };

    //
    // Constructors
    //

    /** Static class, private constructor */
    private ImportUtil()
    {
    }

    public static RE getDelimiterRegexp(String p_delimiter) throws Exception
    {
        // Build a regexp from the separator char and be careful
        // to protect special chars like '|' (make them "\|").

        String pattern;

        if (p_delimiter.equals("tab"))
        {
            pattern = "\t";
        }
        else if (p_delimiter.equals("space"))
        {
            pattern = " ";
        }
        else
        {
            pattern = RE.simplePatternToFullRegularExpression(p_delimiter);
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("delimiter regexp = `" + pattern + "'");
        }

        RE regexp = new RE(pattern);

        return regexp;
    }

    public static boolean isEmptyLine(String p_line)
    {
        return p_line.trim().length() == 0;
    }

    // TODO
    public static boolean isEmptyOrIllegalRow(Cell[] p_rowCells,
            List p_colDescriptors)
    {
        // Should at least contains two non-empty cells to get
        // source and target terms.
        if (p_rowCells.length > 1)
        {
            int index = -1;
            int nonEmptyTerm = 0;
            for (int i = 0; i < p_colDescriptors.size(); i++)
            {
                ColumnDescriptor col = (ColumnDescriptor) p_colDescriptors
                        .get(i);
                index = col.m_position;
                if (col.m_type.equals("term") && index < p_rowCells.length
                        && !isEmptyCell(p_rowCells[index]))
                {
                    nonEmptyTerm++;
                }
                if (nonEmptyTerm > 1)
                {
                    // At least two non-empty term columns.
                    return false;
                }
            }
        }
        return true;

    }

    public static boolean isEmptyCell(Cell p_cell)
    {
        if (p_cell.getContents() == null
                || p_cell.getContents().trim().length() == 0)
        {
            return true;
        }
        return false;
    }

    // If this row contains at least two non-empty cells, we assume it as
    // the start row. start row means it could be entry start from this row.
    public static int determineStartRow(Sheet p_readSheet)
    {
        int totalRows = p_readSheet.getRows();
        Cell[] cells = null;
        int cellLength = 0;
        for (int i = 0; i < totalRows; i++)
        {
            cells = p_readSheet.getRow(i);
            cellLength = cells.length;
            int nonEmptyCell = 0;
            if (cellLength < 2)
            {
                continue;
            }
            for (int j = 0; j < cellLength; j++)
            {
                if (!ImportUtil.isEmptyCell(cells[j]))
                {
                    nonEmptyCell++;
                }
                if (nonEmptyCell > 1)
                {
                    return i;
                }
            }
        }
        return -1;
    }

    // Header row should contain at least two language columns,
    // and these languages should be defined in the specified termbase.
    public static int findHeaderRowNumber(Sheet p_readSheet, int p_fromRow,
            Termbase p_termbase)
    {
        if (p_fromRow < 0)
        {
            return -1;
        }
        Cell[] cells = null;
        for (int i = p_fromRow; i < p_readSheet.getRows(); i++)
        {
            cells = p_readSheet.getRow(i);
            int languageCell = 0;
            if (cells.length < 2)
            {
                // ignor rows that contain less than two columns.
                continue;
            }
            for (int j = 0; j < cells.length; j++)
            {
                if (!isEmptyCell(cells[j])
                        && isLanguageDefined(p_termbase, cells[j].getContents()
                                .trim()))
                {
                    languageCell++;
                }
                if (languageCell > 1)
                {
                    // we have found at least two language columns.
                    return i;
                }
            }
        }
        return -1;

    }

    public static boolean isLanguageDefined(Termbase p_termbase,
            String p_language)
    {
        ArrayList langs = p_termbase.getLanguages();
        Language lang = null;
        for (int i = 0; i < langs.size(); i++)
        {
            lang = (Language) langs.get(i);
            if (lang.getName().equalsIgnoreCase(p_language))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isTypeDefined(String p_type)
    {
        if (p_type == null)
        {
            return false;
        }
        for (int i = 0; i < columnTypes.length; i++)
        {
            if (columnTypes[i].equalsIgnoreCase(p_type))
            {
                return true;
            }
        }
        return false;
    }

    // If a column should associate another column, we just
    // associate it with the first column for now.
    public static int getAssociatedColumn(String p_columnName,
            List p_columnDescriptors)
    {
        int ac = -1;
        if (p_columnName != null)
        {
            if (p_columnName.equalsIgnoreCase("Source")
                    || p_columnName.startsWith("Term"))
            {
                if (p_columnDescriptors.size() > 0)
                {
                    ColumnDescriptor firstColumn = (ColumnDescriptor) p_columnDescriptors
                            .get(0);
                    ac = firstColumn.m_position;
                }
            }
        }
        return ac;
    }

    public static String mapTermbaseDefinedLang(Termbase p_termbase,
            String p_columnName)
    {
        ArrayList langs = p_termbase.getLanguages();
        Language lang = null;
        for (int i = 0; i < langs.size(); i++)
        {
            lang = (Language) langs.get(i);
            if (lang.getName().equalsIgnoreCase(p_columnName))
            {
                return lang.getName();
            }
        }
        return p_columnName;
    }

    /**
     * Remove the illegal char of XML file.
     * 
     * @param p_url
     *            file path name
     * @param p_charsetName
     *            character set name
     */
    public static void filterateXmlIllegal(String p_url, String p_charsetName)
    {
        Reader reader = null;
        File file = new File(p_url);
        if (p_charsetName == null || p_charsetName.length() == 0)
        {
            p_charsetName = "UTF-8";
        }

        try
        {
            reader = new InputStreamReader(new FileInputStream(file),
                    p_charsetName);
            int tempchar;

            String tempUrl = file.getParentFile().toString();
            tempUrl = tempUrl + "\\" + "temp.xml";
            FileOutputStream out = new FileOutputStream(tempUrl);

            while ((tempchar = reader.read()) != -1)
            {
                if (isXMLCharacter((char) tempchar))
                {
                    String s = "" + (char) tempchar;
                    out.write(s.getBytes(p_charsetName));
                }
            }

            reader.close();
            out.flush();
            out.close();

            file.delete();
            File tempFile = new File(tempUrl);
            tempFile.renameTo(new File(p_url));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
     * Check if the char is illegal char of xml file
     */
    public static boolean isXMLCharacter(char c)
    {
        if (c >= 0x20 && c <= 0xD7FF)
            return true;
        if (c >= 0xE000 && c <= 0xFFFD)
            return true;
        if (c >= 0x10000 && c <= 0x10FFFF)
            return true;

        if (c == '\n')
            return true;
        if (c == '\r')
            return true;
        if (c == '\t')
            return true;

        return false;
    }

    /*
     * Remove the xml string illegal char
     */
    public static String removeIllegalXmlChar(String value)
    {
        String newStr = new String();

        for (int i = 0; i < value.length(); ++i)
        {
            if (isXMLCharacter(value.charAt(i)))
            {
                newStr = newStr + value.charAt(i);
            }
        }

        return newStr;
    }
}
