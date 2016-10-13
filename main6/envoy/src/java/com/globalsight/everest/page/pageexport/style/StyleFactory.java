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

/**
 * A factory class that to generate style classes.
 */
public class StyleFactory
{
    public static String DOCX = "docx";
    public static String MIF = "mif";
    public static String PPTX = "pptx";
    public static String XLSX = "xlsx";
    
    /**
     * Gets the style class according to the type.
     * <p>
     * The type should be come from StyleFactory. For example
     * <code>StyleFactory.DOCX</code>
     * 
     * @param type
     *            the type. Come from StyleFactory and used to find the style
     *            class.
     * @return the found style class or null
     */
    public static StyleUtil getStyleUtil(String type)
    {
        if (DOCX.equals(type))
            return new DocxStyleUtil();
        
        if (MIF.equals(type))
        	return new MifStyleUtil();
        
        if (PPTX.equals(type))
        	return new PptxStyleUtil();
        
        if (XLSX.equals(type))
        	return new ExcelStyleUtil();

        return null;
    }
}
