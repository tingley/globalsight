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

package com.globalsight.util.file;

// Core Java clases
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * The DelimitedFileReader can be used to read a delimited text file
 * where comment lines start with a comment string and tokens are delimited
 * by a delimiter.
 */
public class DelimitedFileReader
{

    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get a collection of lines within a file.  The lines within the file
     * are broken based on a given delimiter and returned as an array of strings
     * containing the tokens for each line in the file with values.
     * Comment lines and empty lines are ignored.
     *
     * @param p_fileName - the system-dependent file name (full path required). 
     * @param p_delimiter - The delimiter used for separating tokens of a given line.
     * @param p_commentChar - The argument that starts a single-line comment. 
     */
    public static List readLinesWithTokens(String p_fileName, String p_delimiter,
                                           String p_commentChar)
    throws IOException
    {
        InputStreamReader isr = null;
        String line = null;
        ArrayList list = new ArrayList();

        FileInputStream inputStrm = new FileInputStream(p_fileName);
        isr = new InputStreamReader(inputStrm);

        BufferedReader rd = new BufferedReader(isr);

        while ((line = rd.readLine()) != null)
        {
            if (! (line.startsWith(p_commentChar) || line.trim().equals("")))
            {
                list.add(getTokens(line, p_delimiter));
            }

        }

        rd.close();
        isr.close();

        return list;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Public Methods
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Local Methods
    //////////////////////////////////////////////////////////////////////
    // breake the string into tokens and return them as a String array
    private static String[] getTokens(String p_line, String p_delimiter)
    {
        StringTokenizer st = new StringTokenizer(p_line, p_delimiter);
        int cnt = st.countTokens();

        String[] strings = new String[cnt];
        for (int i=0; i<cnt; i++)
        {
            strings[i] = st.nextToken().trim();
        }

        return strings;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Local Methods
    //////////////////////////////////////////////////////////////////////


    /////////////////  Just for standalone testing......
    public static void main(String[] args)
    {
        try
        {
            List list = readLinesWithTokens("d:\\weblogic\\system4\\serverclasses\\TestFile.txt", "|", "#");
            int size = list.size();
            for (int i=0; i<size; i++)
            {
                System.out.println("----------------------");
                String[] strs = (String[])list.get(i);

                int leng = strs.length;
                for (int j=0; j<leng; j++)
                {
                    System.out.println("TomyD -- value: "+strs[j]);
                }
                System.out.println("----------------------");                
            }
        } catch (Exception e)
        {
            System.out.println("TomyD -- in main we got exception:  "+e);
            e.printStackTrace();
        }
    }
}
