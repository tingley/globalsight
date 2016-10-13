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

package com.globalsight.tools.tmximport;

import java.util.List;
import java.util.ArrayList;

class ParseArgs
{
    private List m_fileList = new ArrayList();
    private String m_tmName = "TMX_import";
    private boolean m_quiet = false;
    private boolean m_delete = false;

    private boolean m_hasError = false;
    private String m_errorMsg;
    
    void parse(String[] args)
    {
        for(int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if(arg.startsWith("-"))
            {
                if(arg.charAt(1) == 't')
                {
                    if(++i >= args.length)
                    {
                        m_hasError = true;
                        m_errorMsg = "Incomplete command line";
                    }
                    m_tmName = args[i];
                }
                else if(arg.charAt(1) == 'q')
                {
                    m_quiet = true;
                }
                else if(arg.charAt(1) == 'd')
                {
                    m_delete = true;
                }
            }
            else
            {
                m_fileList.add(arg);
            }
        }
        
        // mandatry args check
        if(m_fileList.size() == 0)
        {
            m_hasError = true;
            m_errorMsg = "TMX file names must be provided.";
        }
            
    }
    

    boolean isQuiet()
    {
        return m_quiet;
    }

    boolean deletesData()
    {
        return m_delete;
    }

    String getTmName()
    {
        return m_tmName;
    }
    

    List getTmxFileNames()
    {
        return m_fileList;
    }
    

    boolean hasError()
    {
        return m_hasError;
    }
    

    String getErrorMessage()
    {
        return m_errorMsg;
    }
    
}
