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
package com.globalsight.ling.docproc;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Represents an abstract input object for URLs, byte buffers, strings and holds
 * additional data relevant for extraction like the locale and the encoding.
 */
public class EFInputData
{
    // Extraction Options

    /** Input as byte array. */
    private byte[] m_Input = null;

    private File m_file = null;

    /** Input as Unicode string. */
    private String m_strUnicodeInput = null;

    /** Input as URL. */
    private String m_strURL = null;

    /** Java codeset of input. */
    private String m_strCodeset = null;

    private Locale m_Locale = null;

    /**
     * Input type (see {@link IFormatNames} and {@link ExtractorRegistry} for
     * valid input types).
     */
    private int m_iType = EFInputDataConstants.UNKNOWN;

    /** Rules associated with the input. */
    private String m_rules = null;

    // For PPT issue
    private String m_bullets_MsOffice = null;

    /**
     * Excel Do Not Translate configuration
     * 
     */
    private HashMap m_excelStyle = null;

    //
    // Constructor
    //

    public EFInputData()
    {
    }

    //
    // Public Methods
    //

    /**
     * Return the java codeset of the input.
     */
    public String getCodeset()
    {
        return m_strCodeset;
    }

    public byte[] getInput()
    {
        return m_Input;
    }

    public Locale getLocale()
    {
        return m_Locale;
    }

    public int getType()
    {
        return m_iType;
    }

    public String getUnicodeInput()
    {
        return m_strUnicodeInput;
    }

    public String getURL()
    {
        return m_strURL;
    }

    /**
     * Excel Do Not Translate configuration
     * 
     * @param p_excelStyle
     */
    public void setExcelStyle(HashMap p_excelStyle)
    {
        m_excelStyle = p_excelStyle;
    }

    public HashMap getExcelStyle()
    {
        return m_excelStyle;
    }

    /**
     * Sets the java codeset in which the input is given. Use the
     * {@link com.globalsight.ling.common.CodesetMapper CodesetMapper} class to
     * map from IANA encoding to java encodings and back.
     */
    public void setCodeset(String p_strCodeset)
    {
        m_strCodeset = p_strCodeset;
    }

    public void setInput(byte[] p_Input)
    {
        m_Input = p_Input;
    }

    public void setLocale(Locale p_Locale)
    {
        m_Locale = p_Locale;
    }

    public void setType(int p_iType)
    {
        m_iType = p_iType;
    }

    public void setUnicodeInput(String p_strUnicodeInput)
    {
        m_strUnicodeInput = p_strUnicodeInput;
    }

    public void setURL(String p_strURL)
    {
        m_strURL = p_strURL;
        setFile(p_strURL);
    }

    public void setFile(File p_file)
    {
        m_file = p_file;
    }

    private void setFile(String p_strURL)
    {
        String path = p_strURL.replace("file:", "");
        setFile(new File(path));
    }

    public void setRules(String rules)
    {
        m_rules = rules;
    }

    public File getFile()
    {
        return m_file;
    }

    public String getRules()
    {
        return m_rules;
    }

    /**
     * <p>
     * For PPT issue
     * </p>
     */
    public void setBulletsMsOffice(String p_bullets_MsOffice)
    {
        m_bullets_MsOffice = p_bullets_MsOffice;
    }

    public String getBulletsMsOffice()
    {
        return m_bullets_MsOffice;
    }
}
