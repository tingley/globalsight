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
package com.globalsight.everest.corpus;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.persistence.PersistentObject;
import java.io.Serializable;
import java.util.Date;
import com.globalsight.everest.corpus.CorpusDocGroup;

/**
 * A CorpusDoc is a persistent object that corresponds to a row in the
 * corpus_unit_variant table. It contains the binary (native) and GXML versions
 * of a document which are stored in the file storage area.
 */
public class CorpusDoc extends PersistentObject implements Serializable
{
    // ////////////////////////////////////
    // Static Members //
    // ///////////////////////////////////

    private static final long serialVersionUID = -4945946788705993225L;

    /**
     * The relative path prefix for all files (corpus docs) saved to the file
     * storage area using the native file manager
     */
    public static final String REL_PATH_PREFIX = "/GlobalSight/Corpus/";
    private static final String SEP = "/";

    public static final String LOCALE = "m_globalSightLocale";
    public static final String STORE_DATE = "m_storeDate";
    public static final String CORPUS_DOC_GROUP = "m_corpusDocGroup";
    public static final String NATIVE_FORMAT_PATH = "m_nativeFormatPath";

    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private GlobalSightLocale m_globalSightLocale;
    private Date m_storeDate;
    private boolean m_isMapped = false;

    private String m_gxmlPath = null;
    private String m_nativeFormatPath = null;

    private CorpusDocGroup m_corpusDocGroup = null;

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////

    /**
     * Creates a CorpusDoc object
     */
    public CorpusDoc()
    {
        m_globalSightLocale = null;
        m_storeDate = new Date();
        m_isMapped = false;
    }

    // ////////////////////////////////////
    // Public Methods //
    // ////////////////////////////////////

    /**
     * Decides the relative storage path for the corpus doc storage for both
     * GXML and binary files
     * 
     * @param p_isGxml
     *            true if the path is for writing out GXML
     * @return relative path to the appropriate file, relative from the file
     *         storage area
     */
    public String determineStoragePath(boolean p_isGxml)
    {
        String fileName = m_corpusDocGroup.getCorpusName().replace('\\', '/');
        StringBuffer relPath = new StringBuffer(REL_PATH_PREFIX);
        relPath.append(fileName);
        relPath.append(SEP);
        relPath.append(m_corpusDocGroup.getId());
        relPath.append(SEP);
        relPath.append(m_globalSightLocale.toString());
        relPath.append(SEP);
        int idx = fileName.lastIndexOf('/');
        String baseFileName = fileName.substring(idx + 1);
        relPath.append(baseFileName);
        if (p_isGxml)
            relPath.append(".gxml");
        return relPath.toString();
    }

    /**
     * Gets the CorpusDocGroup object that this corpus doc corresponds to.
     * 
     * @return Long
     */
    public CorpusDocGroup getCorpusDocGroup()
    {
        return m_corpusDocGroup;
    }

    /**
     * Sets the corpus_unit (CorpusDocGroup) of the corpus unit variant object
     * that this corpus doc.
     */
    public void setCorpusDocGroup(CorpusDocGroup p_corpusDocGroup)
    {
        m_corpusDocGroup = p_corpusDocGroup;
    }

    /**
     * Returns the locale associated with the document
     * 
     * @return GlobalSightLocale
     */
    public GlobalSightLocale getLocale()
    {
        return m_globalSightLocale;
    }

    /**
     * Sets the locale associated with the document.
     * 
     * @param p_locale
     */
    public void setLocale(GlobalSightLocale p_locale)
    {
        m_globalSightLocale = p_locale;
    }

    /**
     * Returns the date when the corpus doc was created or stored
     * 
     * @return Date
     */
    public Date getStoreDate()
    {
        return m_storeDate;
    }

    /**
     * Sets the date when the corpus doc was created or stored
     * 
     * @param p_storeDate
     */
    public void setStoreDate(Date p_storeDate)
    {
        m_storeDate = p_storeDate;
    }

    /**
     * Returns the path to the GXML file (relative to the file storage area)
     * 
     * @return String
     */
    public String getGxmlPath()
    {
        if (m_gxmlPath == null)
            m_gxmlPath = determineStoragePath(true);
        return m_gxmlPath;
    }

    /**
     * Returns the path to the native format file (relative to the file storage
     * area)
     * 
     * @return String
     */
    public String getNativeFormatPath()
    {
        if (m_nativeFormatPath == null)
            m_nativeFormatPath = determineStoragePath(false);
        return m_nativeFormatPath;
    }

    /**
     * Sets the native format path. This allows re-use of a binary document.
     * 
     * @param p_nativeFormatPath
     */
    public void setNativeFormatPath(String p_nativeFormatPath)
    {
        m_nativeFormatPath = p_nativeFormatPath;
    }

    /**
     * Sets whether the corpus doc has its segments mapped.
     * 
     * @return true | false
     */
    public boolean isMapped()
    {
        return m_isMapped;
    }

    /**
     * Sets whether the corpus doc has had its segments mapped.
     * 
     * @param p_isMapped
     *            true if the corpus mapping has been done
     */
    public void isMapped(boolean p_isMapped)
    {
        m_isMapped = p_isMapped;
    }

    public GlobalSightLocale getGlobalSightLocale()
    {
        return m_globalSightLocale;
    }

    public void setGlobalSightLocale(GlobalSightLocale sightLocale)
    {
        m_globalSightLocale = sightLocale;
    }

    public boolean getIsMapped()
    {
        return m_isMapped;
    }

    public void setIsMapped(boolean mapped)
    {
        m_isMapped = mapped;
    }
}
