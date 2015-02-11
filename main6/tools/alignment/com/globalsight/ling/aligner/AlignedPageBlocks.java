/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

package com.globalsight.ling.aligner;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

public class AlignedPageBlocks
{
    private String m_SourcePath = null;
    private String m_SourceFileName = null;
    private String m_SourceLocale = null;
    private String m_RelativePath = null;
    private List m_PageSegmentList = null;

    /**
     * AlignedPageBlocks constructor comment.
     */
    public AlignedPageBlocks()
    {
        super();
        m_PageSegmentList = new LinkedList();
    }
    
    public void delete()
    {
        m_SourcePath = null;
        m_SourceFileName = null;
        m_SourceLocale = null;
        m_RelativePath = null;
        m_PageSegmentList = null;
    }

    /**
     *
     * @return java.lang.String
     */
    public java.lang.String getSourceFileName()
    {
        return m_SourceFileName;
    }

    /**
     *
     * @return java.lang.String
     */
    public java.lang.String getSourcePath()
    {
        return m_SourcePath;
    }

    /**
     *
     * @param newSourceFileName java.lang.String
     */
    public void setSourceFileName(String p_SourceFileName)
    {
        m_SourceFileName = p_SourceFileName;
    }

    /**
     *
     * @param newSourcePath java.lang.String
     */
    public void setSourcePath(String p_SourcePath)
    {
        m_SourcePath = p_SourcePath;
    }


    /**
     *
     * @return java.util.Iterator
     */
    public Iterator getPageBlocks()
    {
        return m_PageSegmentList.iterator();
    }

    /**
     *
     * @param p_page com.globalsight.ling.aligner.PageBlocks
     */
    public void addPage(PageBlocks p_page)
    {
        m_PageSegmentList.add(p_page);
    }
    
    public String getRelativePath()
    {
        return m_RelativePath;
    }
    
    public void setRelativePath(String p_relativePath)
    {
        m_RelativePath = p_relativePath;
    }
    
    public String getSourceLocale()
    {
        return m_SourceLocale;
    }
    
    public void setSourceLocale(String p_locale)
    {
        m_SourceLocale = p_locale;
    }
    
    public int getPageCount()
    {
        return m_PageSegmentList.size();
    }
    
}