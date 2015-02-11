/*
 Copyright (c) 2000-2005 GlobalSight Corporation. All rights reserved.

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

package com.globalsight.everest.edit.offline.xliff;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;

/**
 * A base class responsible for generating RTF (in Unicode format).
 */
public class XLIFFUnicode implements AmbassadorDwUpConstants
{
    private static final Logger s_logger = Logger
            .getLogger(XLIFFUnicode.class);

    public String m_strEOL = "\r\n";
    public static final char NORMALIZED_LINEBREAK = '\n';

    //
    // Constructor
    //

    public XLIFFUnicode()
    {
    }

}
