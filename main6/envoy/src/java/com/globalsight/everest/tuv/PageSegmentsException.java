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
package com.globalsight.everest.tuv;


//globalsight
import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * An exception handling object for OfflinePageManagement component.
 */

public class PageSegmentsException
    extends GeneralException implements GeneralExceptionConstants
{
    /** takes two arg - the offending segment id */
    public final static String MSG_INVALID_MERGE_PARA_BOUNDARY
        = "InvalidMerge-ParaBoundary";
    /** takes two arg - the offending segment id */
    public final static String MSG_INVALID_MERGE_NON_ADJ_SEGS
        = "InvalidMerge-NonAdjacentSegments";
    /** takes two arg - the offending segment id */
    public final static String MSG_INVALID_MERGE_LOCALIZE_TYPE
        = "InvalidMerge-LocalizeType";
    /** takes one arg - the offending segment id */
    public final static String MSG_INVALID_SPLIT
        = "InvalidSplit-NotMerged";
    /** takes two arg - the offending segment id */
    public final static String MSG_COMPARING_WRONG_TUVS
        = "ComparingWrongTuvs";



    /*
     *
     */
    public PageSegmentsException(Exception p_originalException)
    {
        super( p_originalException );
    }

	
    /*
     *
     */
    public PageSegmentsException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }
}
