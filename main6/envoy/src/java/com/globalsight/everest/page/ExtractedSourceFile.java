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

package com.globalsight.everest.page;


// globalsight
import com.globalsight.everest.page.ExtractedFile;

/**
 * This represents a source file that has been imported into they
 * system and also parsed and extracted.  
 * This class adds any particular data or methods that are particular
 * to a source extracted file and not to a target file.
 */
public class ExtractedSourceFile 
    extends ExtractedFile
{
    //======================================================
    //             private attributes
    //======================================================

    private static final long serialVersionUID = 5943352396382269966L;

    // this is set to true for files with GS tags in 
    // them that allow adding a snippet or deleting content
    // the default is false
    private boolean m_containGsTags = false;

    // the original encoding of the file when imported
    private String  m_originalEncoding = null;
    // the data type of the file like HTML, CSS, etc.
    private String  m_dataType = null;

    /**
     * Default constructor.
     */
    public ExtractedSourceFile()
    {} 

    /**
     * @return the original code set.
     */
    public String getOriginalCodeSet()
    {
        return m_originalEncoding;
    }

    /**
     * @return the native data type.
     */
    public String getDataType()
    {
        return m_dataType;
    }
    
    /**
     * Set the original code set.
     * Does not persist the change.
     * @param p_externalPageId external page identifier.
     */
    public void setOriginalCodeSet(String p_originalCodeSet)
    {
        m_originalEncoding = p_originalCodeSet;
    }

    /**
     * Set the native data type of the file data.
     * Does not persist the change.
     * @param p_dataType the native data type.
     */
    public void setDataType(String p_dataType)
    {
        m_dataType = p_dataType;
    }

    /**
     * Returns whether this file contains any GS tags within it
     * that specifies whether snippets can be added/deleted and
     * specific content can be deleted.
     */
    public boolean containGsTags()
    {
        return m_containGsTags;
    }

    /**
     * Set whether this file contains GS tags in it.
     */
    public void containGsTags(boolean p_containTags)
    {
        m_containGsTags = p_containTags;
    }

    /**
     * Create a new primray file from this one's data.
     */
    public PrimaryFile clonePrimaryFile()
    {
        ExtractedSourceFile pf = new ExtractedSourceFile();
        pf.setInternalBaseHref(getInternalBaseHref());
        pf.setExternalBaseHref(getExternalBaseHref());
        pf.setOriginalCodeSet(getOriginalCodeSet());
        pf.setDataType(getDataType());
        pf.containGsTags(containGsTags());
        return pf;
    }

    /**
     * Return a string representation of the object.
     * @return a string representation of the object.
     */
     public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("m_dataType=");
        sb.append((m_dataType != null ? m_dataType : "null"));
        sb.append(" m_originalEncoding=");
        sb.append((m_originalEncoding != null ? m_originalEncoding : "null"));
        sb.append(" m_containGsTags=");
        sb.append((m_containGsTags == true ? "true" : "false"));
        sb.append("\n");
        return sb.toString();
    }

    public boolean isContainGsTags()
    {
        return m_containGsTags;
    }

    public void setContainGsTags(Boolean gsTags)
    {
        if (gsTags == null)
        {
            m_containGsTags = false;
        }
        else
        {
            m_containGsTags = gsTags.booleanValue();
        }
    }

    public String getOriginalEncoding()
    {
        return m_originalEncoding;
    }

    public void setOriginalEncoding(String encoding)
    {
        m_originalEncoding = encoding;
    }
}

