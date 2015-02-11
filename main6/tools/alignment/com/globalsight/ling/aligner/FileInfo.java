package com.globalsight.ling.aligner;

/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

/**
 * Insert the type's description here.
 */
public class FileInfo
{
    private String m_path;
    private String m_relativePath;
    private String m_name;
    private String m_locale;
    private String m_codeset;

    /**
     * FileInfo constructor comment.
     */
    public FileInfo()
    {
        super();
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/13/2001 2:06:44 PM)
     * @return java.lang.String
     */
    public String getCodeset()
    {
        return m_codeset;
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/13/2001 2:06:44 PM)
     * @return java.lang.String
     */
    public String getLocale()
    {
        return m_locale;
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/13/2001 2:06:44 PM)
     * @return java.lang.String
     */
    public String getPath()
    {
        return m_path;
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/13/2001 2:06:44 PM)
     * @param newM_path java.lang.String
     */
    public String getRelativePath(String p_path)
    {
        return m_relativePath;
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/13/2001 2:06:44 PM)
     * @param newM_codeset java.lang.String
     */
    public void setCodeset(String p_codeset)
    {
        m_codeset = p_codeset;
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/13/2001 2:06:44 PM)
     * @param newM_locale java.lang.String
     */
    public void setLocale(String p_locale)
    {
        m_locale = p_locale;
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/13/2001 2:06:44 PM)
     * @param newM_path java.lang.String
     */
    public void setPath(String p_path)
    {
        m_path = p_path;
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/13/2001 2:06:44 PM)
     * @param newM_path java.lang.String
     */
    public void setRelativePath(String p_path)
    {
        m_relativePath = p_path;
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/13/2001 2:06:44 PM)
     * @param newM_path java.lang.String
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }


    /**
     * Insert the method's description here.
     * Creation date: (2/13/2001 2:06:44 PM)
     * @param newM_path java.lang.String
     */
    public String getName()
    {
        return m_name;
    }
}