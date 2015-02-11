package com.globalsight.ling.aligner;
import java.util.Iterator;

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

import java.util.Locale;
import java.util.Vector;

/**
 * A data object containing all the information needed to
 * run the aligner on several target directories.
 */
public class AlignedFiles
{
    private String m_srcLocale = null;
    private String m_srcCodeset = null;
    private String m_srcPath = null;
    private String m_srcRelativePath = null;
    private String m_srcFileName = null;
    private String m_jdbcDriver = null;
    private String m_dbUrl = null;
    private String m_dbImportRootDir = null;
    private boolean m_insertModifiedSegsOnly = false;
    private String m_userName = null;
    private String m_userPswd = null;
    
    private String m_fileFormat = null;
    private String m_fileExtList = null;

    private Vector m_trgFileList = new Vector();

    /**
     * AlignedFiles constructor comment.
     */
    public AlignedFiles()
    {
        super();
    }

    /**
     * Returns the native file format
     * @return java.lang.String
     */
    public String getFormat()
    {
        return m_fileFormat;
    }

    /**
     * returns the source codeset
     * @return String codeset
     */
    public String getSrcCodeset()
    {
        return m_srcCodeset;
    }

    /**
     * returns src locale
     * @return String 
     */
    public String getSrcLocale()
    {
        return m_srcLocale;
    }

    /**
     * return the source path
     * @return java.lang.String
     */
    public String getSrcPath()
    {
        return m_srcPath;
    }

    /**
     * gets the source relative path
     */
    public String getSrcRelativePath()
    {
       return m_srcRelativePath;
    }

    /**
     * gets the source file name
     */
    public String getSrcFileName()
    {
      return  m_srcFileName;
    }

    /**
     * gets db url
     */
    public String getDbUrl()
    {
       return m_dbUrl;
    }

    /**
     * gets jdbc driver name
     */
    public String getJdbcDriver()
    {
       return m_jdbcDriver;
    }

    /**
     * get user name
     */
    public String getUserName()
    {
       return m_userName;
    }

    /**
     * get user password
     */
    public String getUserPswd()
    {
       return m_userPswd;
    }

    /**
     * returns Target FileInfo iterator
     * @return java.util.Iterrator
     */
    public Iterator getTrgFileIterator()
    {
        if( m_trgFileList != null )
        {
            return m_trgFileList.iterator();
        }
        else
        {
            return null;
        }
    }

    /**
     * returns Target FileInfo iterator
     * @return java.util.Iterrator
     */
    public void addTrgFileInfo(FileInfo p_info )
    {
        if( m_trgFileList != null )
        {
            m_trgFileList.addElement( p_info );
        }
    }

    /**
     * sets the native file format
     */
    public void setFormat(String p_fmt )
    {
       m_fileFormat = p_fmt;
    }

    /**
     * sets the source codeset
     */
    public void setSrcCodeset( String p_codeset )
    {
        m_srcCodeset = p_codeset;
    }

    /**
     * sets the source local;
     */
    public void setSrcLocale( String p_locale )
    {
       m_srcLocale = p_locale;
    }

    /**
     * sets the source path
     */
    public void setSrcPath(String p_path)
    {
       m_srcPath = p_path ;
    }

    /**
     * sets the source relative path
     */
    public void setSrcRelativePath(String p_path)
    {
       m_srcRelativePath = p_path ;
    }

    /**
     * sets the source file name
     */
    public void setSrcFileName(String p_name)
    {
       m_srcFileName = p_name;
    }

    /**
     * sets jdbc driver name
     */
    public void setJdbcDriver(String p_name)
    {
       m_jdbcDriver = p_name;
    }

    /**
     * sets db url
     */
    public void setDbUrl(String p_name)
    {
       m_dbUrl = p_name;
    }

    /**
     * sets user name
     */
    public void setUserName(String p_name)
    {
       m_userName = p_name;
    }

    /**
     * sets user password
     */
    public void setUserPswd(String p_name)
    {
       m_userPswd = p_name;
    }

    /**
     * get the database import directory
     */
    public String getImportRootDir()
    {
       return m_dbImportRootDir;
    }

    /**
     * sets database import directory
     */
    public void setImportRootDir(String p_name)
    {
       m_dbImportRootDir = p_name;
    }

    /**
     * Sets a flag which requests that only modified segments
     * be commited to the database.
     */
    public void setInsertModifiedSegsOnly()
    {
       m_insertModifiedSegsOnly = true;
    }

    /**
     * Confirms the request to insert only the modified segments into the database. 
     */
    public boolean isInsertModifiedSegsOnly()
    {
       return m_insertModifiedSegsOnly;
    }

    /**
     * Enables/Disables the request that only modified segments
     * be commited to the database.
     *
     * @param p_mode when true, only modified segments are updated.
     */
    public void setInsertModifiedSegsOnly(boolean p_mode)
    {
       m_insertModifiedSegsOnly = p_mode;
    }
}