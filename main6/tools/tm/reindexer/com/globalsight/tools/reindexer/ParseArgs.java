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

package com.globalsight.tools.reindexer;


class ParseArgs
{
    private boolean m_quiet = false;
    private boolean m_initRecordTable = false;
    private boolean m_verbose = false;
    private long m_tokenBufferSize = 600000;
    
    private boolean m_hasError = false;
    private String m_errorMsg;
    
    void parse(String[] args)
    {
        for(int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if(arg.startsWith("-"))
            {
                if(arg.charAt(1) == 'q')
                {
                    m_quiet = true;
                }
                else if(arg.charAt(1) == 'i')
                {
                    m_initRecordTable = true;
                }
                else if(arg.charAt(1) == 'v')
                {
                    m_verbose = true;
                }
                else
                {
                    if(++i >= args.length)
                    {
                        m_hasError = true;
                        m_errorMsg = "Incomplete command line";
                    }
                    else
                    {
                        String value = args[i];
                        
                        if(arg.charAt(1) == 't')
                        {
                            m_tokenBufferSize = Long.parseLong(value);
                        }
                        else
                        {
                            m_hasError = true;
                            m_errorMsg = "Unrecognized option: "
                                + arg + " " + value;
                        }
                    }
                    
                }
            }
            else
            {
                m_hasError = true;
                m_errorMsg = "Unrecognized option: " + arg;
            }
        }
            
    }

    boolean hasError()
    {
        return m_hasError;
    }
    

    boolean isQuiet()
    {
        return m_quiet;
    }

    boolean initRecordTable()
    {
        return m_initRecordTable;
    }

    boolean isDebug()
    {
        return m_verbose;
    }

    long tokenBufferSize()
    {
        return m_tokenBufferSize;
    }
    
    String getErrorMessage()
    {
        return m_errorMsg;
    }
}
