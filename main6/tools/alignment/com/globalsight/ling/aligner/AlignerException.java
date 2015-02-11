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

import com.globalsight.ling.util.GeneralException;

public class AlignerException
  extends GeneralException
{

    /**
     *
     */
    public AlignerException(int p_exceptionId)
    {
        super(-1, p_exceptionId);
    }

    /**
     *
     * @param s java.lang.String
     */
    public AlignerException(int p_exceptionId, Exception p_originalException)
    {
        super(-1, p_exceptionId, p_originalException);
    }

    /**
     * Constructs an instance using the given component and exception
     * identification.
     *
     * @param p_exceptionId Reason for the exception.
     * @param p_message Explanation of the exception.
     */
    public AlignerException(int p_exceptionId, String p_message)
    {
        super(-1, p_exceptionId, p_message);
    }

    /**
     * Returns a String that represents the value of this object.
     * @return a string representation of the receiver
     */
    public String toString()
    {
        // Insert code to print the receiver here.
        // This implementation forwards the message to super. You may replace or supplement this.
        return super.toString();
    }
}