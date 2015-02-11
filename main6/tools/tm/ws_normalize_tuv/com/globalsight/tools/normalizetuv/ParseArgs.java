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

package com.globalsight.tools.normalizetuv;


class ParseArgs
{
    private String m_sourceLocale = null;
    private boolean m_askProceed = true;
    
    void parse(String[] args)
        throws Exception
    {
        for(int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if(arg.startsWith("-"))
            {
                if(arg.indexOf('q') != -1)
                {
                    m_askProceed = false;
                }
            }
            else
            {
                m_sourceLocale = arg;
                break;
            }
        }
        
        // locale string error check
        if(m_sourceLocale == null
            || m_sourceLocale.length() != 5
            || !m_sourceLocale.substring(2, 3).equals("_"))
        {
            throw new Exception("Source locale name is incorrect: "
                + m_sourceLocale);
        }
    }
    

    String getSourceLocale()
    {
        return m_sourceLocale;
    }
    

    boolean askProceed()
    {
        return m_askProceed;
    }
}
