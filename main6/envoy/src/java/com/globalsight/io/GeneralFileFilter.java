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
package com.globalsight.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 * This class is used to filter files.
 */
public class GeneralFileFilter implements FileFilter 
{
    private String[] m_patterns = null;
    private int m_patternOperator = -1;
    private boolean m_isIgnoreCase = true;
    private boolean m_isRecurseDirectories = false;

    /**
     * The file name must start with one of the patterns
     * for accept to return true.
     */
    public static final int STARTS_WITH_PATTERN_OPERATOR = 1;
    /**
     * The file name must end with one of the patterns
     * for accept to return true.
     */
    public static final int ENDS_WITH_PATTERN_OPERATOR = 2;
    /**
     * The file name must contain one of the patterns
     * for accept to return true.
     */
    public static final int CONTAINS_PATTERN_OPERATOR = 3; 

    /**
     * Ignore case in determining if accept should return true.
     */
    public static final boolean IGNORE_CASE = true;

    /**
     * Recurse into directories and test files there.
     */
    public static final boolean RECURSE_DIRECTORIES = true;
    
    
  
    /**
     * Implements FileFilter with the set of patterns, 
     * pattern operators, and whether to respect or ignore case.
     * @param p_filePatterns - array of Strings that are patterns
     * to find in file names.
     * @param p_patternOperator - the pattern operator to apply.
     * One of STARTS_WITH_PATTERN_OPERATOR, 
     * ENDS_WITH_PATTERN_OPERATOR, or 
     * CONTAINS_PATTERN_OPERATOR.
     * @param p_isIgnoreCase - true means ignore case in pattern
     * matching.
     * @param p_isRecurseDirectories - true means recurse into 
     * directories and test files there.
     */  
    public GeneralFileFilter(String[] p_filePatterns, 
            int p_patternOperator, boolean p_isIgnoreCase,
            boolean p_isRecurseDirectories) 
    {
        switch (p_patternOperator)
        {
            case STARTS_WITH_PATTERN_OPERATOR:
            case ENDS_WITH_PATTERN_OPERATOR:
            case CONTAINS_PATTERN_OPERATOR: 
                break;
            default:
                throw new RuntimeException("Invalid p_patternOperator "
                        + Integer.toString(p_patternOperator));
        }
        m_patternOperator = p_patternOperator; 
        m_isIgnoreCase = p_isIgnoreCase;
        m_isRecurseDirectories = p_isRecurseDirectories;
	    setUpFilePatterns(p_filePatterns);
    }
    
    //
    // FileFilter interface implementation
    //
    
    /**
     * Tests whether or not the specified abstract pathname 
     * should be included in a pathname list.
     * @param p_pathName - The abstract pathname to be tested
     * @returns:
     * true if and only if pathname should be included
     */
    public boolean accept(File p_pathName)  
    {
	    if (p_pathName.isFile()) 
        {
	        String fileName = p_pathName.getName();
            if (m_isIgnoreCase)
            {
                // Ignore case compares lower case.
                fileName = fileName.toLowerCase();
            }
            for (int i = 0; i < m_patterns.length; i++)
            {
                switch (m_patternOperator)
                {
                    case STARTS_WITH_PATTERN_OPERATOR:
	                    if (fileName.startsWith(m_patterns[i]))
                        {
                            return true;
                        }
                        break; 
                    case ENDS_WITH_PATTERN_OPERATOR:
	                    if (fileName.endsWith(m_patterns[i]))
                        {
                            return true;
                        }
                        break;
                    case CONTAINS_PATTERN_OPERATOR:
	                    if (fileName.indexOf(m_patterns[i]) > -1)
                        {
                            return true;
                        }
                        break;
                } 
	        }
	    } 
        else 
        {
            if ( ! m_isRecurseDirectories )
            {
                return false;
            }
            if (p_pathName.isDirectory()) 
            {
	            File[] directoryContent = p_pathName.listFiles();
	            if (directoryContent != null 
                        && directoryContent.length != 0)
                {
		            for (int i=directoryContent.length-1; i >= 0; i--) 
                    {
		                if (accept(directoryContent[i]))
                        {
			                return true;
                        }
                    }
                }
            }
		}
	    return false;
    }


    //
    // public methods
    //
    
    public String toString()
    {
        return super.toString()
                + " m_patterns=" + (m_patterns!=null?
                Arrays.asList(m_patterns).toString():"null")
                + " m_patternOperator=" 
                + Integer.toString(m_patternOperator)
                + " m_isIgnoreCase=" 
                + new Boolean(m_isIgnoreCase).toString()
                + " m_isRecurseDirectories=" 
                + new Boolean(m_isRecurseDirectories).toString()
                ;
    }


    //
    // private methods
    //


    // place all the file patterns in m_patterns.
    private void setUpFilePatterns(String[] p_filePatterns) 
    {
        if (p_filePatterns.length == 0)
        {
            throw new RuntimeException("p_filePatterns empty");
        }
        m_patterns = new String[p_filePatterns.length];
	    for (int i=0; i < p_filePatterns.length; i++) 
        {
            if (p_filePatterns[i] == null 
                    || p_filePatterns[i].length() == 0)
            {
                throw new RuntimeException(
                        "p_filePatterns has empty string");
            }
            // if ignore case, compare lower case file patterns
	        m_patterns[i] = (m_isIgnoreCase?
                    p_filePatterns[i].toLowerCase()
                    :p_filePatterns[i]); 
	    }
    }
}
