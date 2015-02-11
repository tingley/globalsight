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

package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This class reads a property file and stores the key, value, and comment.
 * The objects are also stored in the same order in which they were read.
 * A comment immediately preceeding a key is considered that key's comment.
 */
public class PropertyList
{
    private static int KEY = 0;
    private static int VALUE = 1;
    private static int COMMENT = 2;
    private static String NEWLINE = System.getProperty("line.separator");
    
    private ArrayList<String>[] m_properties = new ArrayList[3];

    /**
     * Adds the key, value and comment to the list.
     */
    public void add(String p_key, String p_comment, String p_value)
    {
        m_properties[KEY].add(p_key);
        m_properties[COMMENT].add(p_comment);
        m_properties[VALUE].add(p_value);
    }
    
    /**
     * Returns whether the list contains the key.
     */
    public boolean containsKey(String p_key)
    {
        int index = (m_properties[KEY]).indexOf(p_key);
        return (index != -1);
    }
    
    private String getItem(String p_key, int whichItem)
    {
        int index = (m_properties[KEY]).indexOf(p_key);
        if (index != -1) {
            return (String) (m_properties[whichItem]).get(index);
        }
        else {
            return null;
        }
    }
    
    /**
     * Returns the comment based on the key.
     */
    public String getComment(String p_key)
    {
        return getItem(p_key, COMMENT);
    }
    
    /**
     * Returns the value based on the key.
     */
    public String getValue(String p_key)
    {
        return getItem(p_key, VALUE);
    }
    
    /**
     * Compares two values.  For comma separated lists, having no space
     * or one space after the comma is equivalent.
     */
    public static boolean compareValues(String p_value1, String p_value2)
    {
        p_value1 = p_value1.replaceAll(", ", ",");
        p_value2 = p_value2.replaceAll(", ", ",");

        return p_value1.equals(p_value2);
    }
    
    /**
     * Returns the key based on the index.
     */
    public String getKey(int index)
    {
        return m_properties[KEY].get(index);
    }
    
    /**
     * Returns the comment based on the index.
     */
    public String getComment(int index)
    {
        return m_properties[COMMENT].get(index);
    }
    
    /**
     * Returns the value based on the index.
     */
    public String getValue(int index)
    {
        return m_properties[VALUE].get(index);
    }
    
    /**
     * Removes the key, value, and comment, based on the key.
     */
    public void remove(String p_key)
    {
        int index = m_properties[KEY].indexOf(p_key);
        if (index != -1) {
            m_properties[KEY].remove(index);
            m_properties[VALUE].remove(index);
            m_properties[COMMENT].remove(index);
        }
    }
    
    /**
     * Returns the size of the list.
     */
    public int size()
    {
        return m_properties[KEY].size();
    }
    
    /**
     * Loads the properties from the specified file.
     */
    private void loadPropertyFile(Properties p_properties, File p_propertyFile)
    throws IOException
    {
        FileInputStream is = new FileInputStream(p_propertyFile);
        p_properties.load(is);
        is.close();
    }
    
    /**
     * Parse a list into an array list.
     */
    public static ArrayList<String> parseList(String p_listOfValues)
    {
        ArrayList<String> values = new ArrayList<String>();
        parseList(p_listOfValues, values);
        return values;
    }

    /**
     * Parse a list into an array list.  If the list contains new lines (\n),
     * that will be used as delimiters.  Otherwise, break on commas.
     */
    public static void parseList(String p_listOfValues, ArrayList<String> p_values)
    {
        if (p_listOfValues == null)
            return;

        String breakMark = (p_listOfValues.indexOf("\n") != -1 ?
                                "\n" : ",");

        StringTokenizer st = new StringTokenizer(p_listOfValues,  breakMark);
        while (st.hasMoreTokens()) {
            p_values.add(st.nextToken().trim());
        }
    }

    /**
     * Constructor, which reads a property file and retrieves the keys, values, and comments.
     * @param p_sourceFile property file.
     * @throws IOException
     */
    public PropertyList(File p_sourceFile)
    throws IOException
    {
        m_properties = new ArrayList[3];
        for (int i = 0; i < m_properties.length; i++) {
            m_properties[i] = new ArrayList<String>();
        }

        Properties oldProperties = new Properties();
        loadPropertyFile(oldProperties, p_sourceFile);       // Load the source file on top of it

        BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(p_sourceFile), "ISO-8859-1"));
        
        String comment = "";
        String line;
        // Read each line the destination file
        while ((line = in.readLine()) != null) {
            // If the line is a blank, write it out as is
            String trimmed = line.trim();
            if (trimmed.equals("")) {
                comment = "";
            }
            // If the line is a comment, write it out as is, and save the
            // comment in the variable.
            else if (trimmed.startsWith("#") || trimmed.startsWith("!")) {
                comment += line + NEWLINE;
            }
            // Otherwise, extract the key from the line, then retrieve
            // its value, and write them out to the file.
            else {
                StringTokenizer st = new StringTokenizer(line, "=: \t");
                if (st.hasMoreTokens()) {
                    String key = st.nextToken().trim();
                    if (oldProperties.containsKey(key)) {
                        String value = oldProperties.getProperty(key);
                        
                        add(key, comment, value);
                    }

                    // Since we only care about the key in the destination file,
                    // ignore the continued values.
                    while (line != null && line.endsWith("\\") && !line.endsWith("\\\\"))
                        line = in.readLine();
                    comment = "";
                }
            }
        }
        in.close();
    }
}