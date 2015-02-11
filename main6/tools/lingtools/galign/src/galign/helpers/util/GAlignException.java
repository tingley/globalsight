/* Copyright (c) 2004, GlobalSight Corporation.  All rights reserved.
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

package galign.helpers.util;

import galign.Setup;

public class GAlignException extends Exception
{
    private String m_msg;

    public GAlignException(String p_key, Exception p_original)
    {
        super();
        String msg = Setup.getError(p_key);
        m_msg = msg + " (" + p_original.getMessage() + ")";
    }

    public GAlignException(String p_key)
    {
        super();
        m_msg = Setup.getError(p_key);
    }

    public String getMessage()
    {
        return m_msg;
    }
}
