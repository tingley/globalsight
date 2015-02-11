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
package com.globalsight.cxe.persistence.cms.teamsitedbmgr;
/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved.
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
import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;
/**
 * An exception thrown during the process of accessing the database.
 * <p>
 * For this jobhandler component the EXCEPTION ID ranges from 1200-1299.
 * For this jobhandler component the ERROR message ID ranges from 3200-3299.
 * For this jobhandler component the INFO message ID ranges from 5200-5299.
 * For this jobhandler component the DEBUG message ID ranges from 7200-7299.
 * <p>
 * Creation date: (8/20/00)
 * @author: Richard Lipes
 */
/*
 * MODIFIED MM/DD/YYYY
 * rlipes   04/19/2001 Initial version.
 */
public class DBException extends GeneralException implements GeneralExceptionConstants
{
    public final static int EX_NO_DATABASE_CONNECTION 		= 1206;
    public final static int EX_PERSISTENCE			= 1209;
    public final static int EX_DB_ERROR        			= 1250;

    public final static int EX_BRANCH_ALREADY_EXISTS 		= 1214;
    public final static int MSG_FAILED_TO_CREATE_BRANCH	= 3204;
    public final static int MSG_FAILED_TO_GET_ALL_BRANCHES	= 3208;
    public final static int MSG_FAILED_TO_REMOVE_BRANCH	= 3217;

    /**
     * Constructs an instance of DBException using the exception identification.
     * <p>
     * @param p_exceptionId The id for the type of exception.
     */
    public DBException(int p_exceptionId)
    {
	super(COMP_JOBS, p_exceptionId);
    }

    /**
     * Constructs an instance of DBException using the exception identification,
     * and original exception.
     * <p>
     * @param p_exceptionId The id for the type of exception.
     * @param p_originalException The originating exception.
     */
    public DBException(int p_exceptionId, Exception p_originalException)
    {
        super(COMP_JOBS, p_exceptionId, p_originalException);
    }

    /**
     * Constructs an instance of DBException using the exception identification,
     * and the error message.
     * <p>
     * @param p_exceptionId The id for the type of exception.
     * @param p_errorMessage The message that tells more detail about the exception.
     */
    public DBException(int p_exceptionId, String p_errorMessage)
    {
        super(COMP_JOBS, p_exceptionId, p_errorMessage);
    }

    /**
     * Constructs an instance of DBException using the exception identification,
     * and the error message id.
     * <p>
     * @param p_exceptionId The id for the type of exception.
     * @param p_messageId The id for the explanation of the exception.
     */
    public DBException(int p_exceptionId, int p_messageId)
    {
	super(COMP_JOBS, p_exceptionId, p_messageId);
    }

    /**
     * Constructs an instance of DBException using the exception identification,
     * message identification, and the originating exception.
     * <p>
     * @param p_exceptionId The id for the type of exception.
     * @param p_messageId The id for the explanation of the exception.
     * @param p_originalException The originating exception that this exception identifies.
     */
    public DBException(int p_exceptionId, int p_messageId, Exception p_originalException)
    {
	super(COMP_JOBS, p_exceptionId, p_messageId, p_originalException);
    }

    /**
     * Constructs an instance of DBException using the exception identification,
     * message identification, and the original exception.
     * NOTE: THIS CONSTRUCTOR IS USED FOR COMPOUND ERROR MESSAGES.
     * <p>
     * @param p_exceptionId The id for the type of exception.
     * @param p_messageId The id for the explanation of the exception.
     * @param p_messageArguments The message arguments in order.
     * @param p_originalException Original exception that this exception edentifies.
     */
    public DBException(int p_exceptionId, int p_messageId,
			String[] p_messageArguments, Exception p_originalException)
    {
	super(COMP_JOBS, p_exceptionId, p_messageId, p_messageArguments, p_originalException);
    }
}
