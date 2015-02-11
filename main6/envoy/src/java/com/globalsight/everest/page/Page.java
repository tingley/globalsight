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

// globalsight imports
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * The Page abstract class represents a display page of data to import, edit,
 * and export. It's used as an abstraction for a source and a target page.
 */
public abstract class Page extends PersistentObject implements GenericPage
{
    /**
     * Constant used for TopLink's query. The constant value has to be exactly
     * the same as the variable defined as the id of a page (for mapping
     * purposes).
     */
    public static final String ID = M_ID;
    public static final String STATE = "m_pageState";

    /**
     * Constant used for TopLink's query. The constant value has to be exactly
     * the same as the variable defined as the external page id of a page (for
     * mapping purposes).
     */
    public static final String EXTERNAL_PAGE_ID = "m_externalPageId";

    /**
     * Constant used for TopLink's query. The constant value has to be exactly
     * the same as the variable defined as the id of a page (for mapping
     * purposes). This is the corpus_unit_variant.id
     */
    public static final String CUV_ID = "m_cuvId";

    private String m_pageState = PageState.IMPORTING;
    private String m_externalPageId = null;
    private Long m_cuvId = null;
    private String m_dataSourceType = null;
    protected GlobalSightLocale m_globalSightLocale = null;
    private String m_prevStateBeforeUpdate = null;

    // both of these are needed in order to
    // accomodate using an aggregate object mapping for
    // TOPLink mapping. One of these will always be NULL.
    // A page can only have one Primary File - which will be an unextracted
    // OR an extracted file but not both.
    protected UnextractedFile m_unextractedFile = null;
    protected ExtractedFile m_extractedFile = null;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constructor.
     */
    public Page()
    {
        super();
    }

    /**
     * Constructor used to just set up the primary file.
     */
    public Page(int p_pageType)
    {
        createPrimaryFile(p_pageType);
    }

    /**
     * Constructed through a SourcePage or TargetPage.
     * 
     * @param p_externalPageId -
     *            The external page id (fully qualified path).
     * @param p_globalSightLocale -
     *            The locale of the page.
     * @param p_originalEncoding -
     *            The original encoding of the page.
     * @param p_dataSourceType -
     *            The datasource type of the page (i.e. File-based).
     * @param p_dataType -
     *            The data type of the page (i.e. html, xml , and etc.)
     */
    Page(String p_externalPageId, GlobalSightLocale p_globalSightLocale,
            String p_dataSourceType, int p_pageType)
    {
        super();
        createPrimaryFile(p_pageType);

        m_externalPageId = p_externalPageId;
//        m_externalPageId = m_externalPageId.replace('\\', '/');
        
        m_globalSightLocale = p_globalSightLocale;
        m_dataSourceType = p_dataSourceType;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Abstract Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Get the locale id of the Page.
     * 
     * @return the Locale id of the page.
     */
    abstract public long getLocaleId();

    /*
     * Return the locale associated with the page.
     */
    abstract public GlobalSightLocale getGlobalSightLocale();

    // ////////////////////////////////////////////////////////////////////
    // End: Abstract Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Public Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Set the state of the page to be the specified value.
     * 
     * @param p_state -
     *            the state to be set.
     */
    public void setPageState(String p_state)
    {
        if (p_state == null)
        {
            return;
        }
        // if setting to UPDATING - first set the previous state
        if (PageState.UPDATING.equals(p_state))
        {
            m_prevStateBeforeUpdate = m_pageState;
        }
        // of if it was in UPDATING but not anymore
        // clear out the previous state
        else if (PageState.UPDATING.equals(m_pageState)
                && !PageState.UPDATING.equals(p_state))
        {
            m_prevStateBeforeUpdate = null;
        }

        m_pageState = p_state;
    }

    /**
     * Get the page state.
     * 
     * @return The state of this page.
     */
    public String getPageState()
    {
        return m_pageState;
    }

    /**
     * Get the state of the page before it was updated. This will only return a
     * state if the "getPageState()" returns UPDATING - otherwise returns NULL.
     */
    public String getPageStateBeforeUpdating()
    {
        return m_prevStateBeforeUpdate;
    }

    /**
     * Returns the unique page identifier for the page external to the system.
     * 
     * @return the external page identifier.
     */
    public String getExternalPageId()
    {
        return m_externalPageId;
    }

    /**
     * Set the external page id.
     * 
     * @param p_externalPageId -
     *            The external page id to be set.
     */
    public void setExternalPageId(String p_externalPageId)
    {
        m_externalPageId = p_externalPageId;
    }

    /**
     * Returns the corpus_unit_variant ID for the associated page in the corpus
     * TM.
     * 
     * @return cuvId
     */
    public Long getCuvId()
    {
        return m_cuvId;
    }

    /**
     * Set the corpus_unit_variant ID for the associated page in the corpus TM.
     * 
     * @param p_cuvId --
     *            the new page in the corpus to which this page corresponds
     */
    public void setCuvId(Long p_cuvId)
    {
        m_cuvId = p_cuvId;
    }

    /**
     * Set the locale id of the page. Does not persist the change.
     * 
     * @param p_localeId
     *            the locale to set the page to.
     */
    public void setGlobalSightLocale(GlobalSightLocale p_globalSightLocale)
    {
        m_globalSightLocale = p_globalSightLocale;
    }

    /**
     * Set the data source type. Does not persist the change.
     * 
     * @param p_dataSourceType
     *            the data source type.
     */
    public void setDataSourceType(String p_dataSourceType)
    {
        m_dataSourceType = p_dataSourceType;
    }

    /**
     * @return the data source type.
     */
    public String getDataSourceType()
    {
        return m_dataSourceType;
    }

    /*
     * Returns the primary file that the page is associated with. Must check the
     * two variables because the TOPLink mapping needs the two variables.
     */
    public PrimaryFile getPrimaryFile()
    {
        PrimaryFile file = null;
        
        if (m_unextractedFile != null && !m_unextractedFile.isEmpty())
        {
            file = m_unextractedFile;
        }
        else if (m_extractedFile == null && m_unextractedFile != null)
        {
            file = m_unextractedFile;
        }
        else
        {
            if (m_extractedFile != null && m_extractedFile.getTemplateMap().size() == 0
                    && this instanceof TargetPage)
            {
                Map templates = ((TargetPage) this).getSourcePage()
                        .getExtractedFile().getTemplates();
                
                if (templates == null)
                {
                	String hql = "from PageTemplate p where p.sourcePage.id = ?";
					List<PageTemplate> ts = (List<PageTemplate>) HibernateUtil
							.search(hql, ((TargetPage) this).getSourcePage()
									.getIdAsLong());
					
					templates = new HashMap<Long, PageTemplate>();
                    for (PageTemplate t : ts)
                    {
                    	templates.put(new Long(t.getType()), t);
                    }
                }
                m_extractedFile.setTemplates(templates);
            }
            file = m_extractedFile;
        }

        return file;       
    }

    /**
     * Return the type of primary file this page is associated with. It must
     * check the two variables because the TOPLink mapping needs tht two
     * variables.
     */
    public int getPrimaryFileType()
    {
        if (m_unextractedFile != null && !m_unextractedFile.isEmpty())
        {
            return PrimaryFile.UNEXTRACTED_FILE;
        }

        return PrimaryFile.EXTRACTED_FILE;
    }

    /**
     * Determines whether the page is in updating state. This state is set when
     * its GXML is being updated and the process has not completed yet.
     */
    public boolean isInUpdatingState()
    {
        return PageState.UPDATING.equals(m_pageState);
    }

    /**
     * Should be overriden by the subclasses and create the primary file of the
     * appropriate type.
     */
    protected abstract void createPrimaryFile(int p_type);

    /**
     * The primary file can only be set by this class or any of its subclasses.
     */
    protected void setPrimaryFile(PrimaryFile p_pf)
    {
        switch (p_pf.getType())
        {
        case PrimaryFile.UNEXTRACTED_FILE:
            m_unextractedFile = (UnextractedFile) p_pf;
            m_extractedFile = null;
            break;
        case PrimaryFile.EXTRACTED_FILE:
        default:
            m_extractedFile = (ExtractedFile) p_pf;
            m_unextractedFile = null;
            break;
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Public Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Override Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append(" m_externalPageId=");
        sb.append((m_externalPageId != null ? m_externalPageId : "null"));
        sb.append("\nm_globalSightLocale=");
        sb.append((m_globalSightLocale != null ? m_globalSightLocale
                .toDebugString() : "null"));
        sb.append(" m_dataSourceType=");
        sb.append((m_dataSourceType != null ? m_dataSourceType : "null"));
        sb.append(" m_pageState=");
        sb.append(m_pageState != null ? m_pageState : "null");
        sb.append("\n");
        sb.append(" m_cuvId=").append(m_cuvId).append("\n");
        sb.append(getPrimaryFile().toString());

        return sb.toString();
    }

    public UnextractedFile getUnextractedFile()
    {
        return m_unextractedFile;
    }

    public void setUnextractedFile(UnextractedFile file)
    {
        m_unextractedFile = file;
    }

    public ExtractedFile getExtractedFile()
    {
        return m_extractedFile;
    }

    public void setExtractedFile(ExtractedFile file)
    {
        m_extractedFile = file;
    }

    public String getPrevStateBeforeUpdate()
    {
        return m_prevStateBeforeUpdate;
    }

    public void setPrevStateBeforeUpdate(String stateBeforeUpdate)
    {
        m_prevStateBeforeUpdate = stateBeforeUpdate;
    }  
    
    public String getShortPageName()
    {
        String name = this.getExternalPageId();
        if (name == null)
        {
            return "";
        }
        
        File file = new File(name);
        return file.getName() + getSubFileName(name);
    }
    
    public String getDisplayPageName()
    {
        String name = this.getExternalPageId();
        if (name == null)
        {
            return "";
        }
        
        return getMainFileName(name) + getSubFileName(name);
    }
    
    private String getMainFileName(String p_filename)
    {
        int index = p_filename.indexOf(")");
        if (index > 0 && p_filename.startsWith("("))
        {
            index++;
            while (Character.isWhitespace(p_filename.charAt(index)))
            {
                index++;
            }

            return p_filename.substring(index, p_filename.length());
        }

        return p_filename;
    }

    private String getSubFileName(String p_filename)
    {
        int index = p_filename.indexOf(")");
        if (index > 0 && p_filename.startsWith("("))
        {
            return " " + p_filename.substring(0, p_filename.indexOf(")") + 1);
        }

        return "";
    }
}
