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
package com.globalsight.everest.util.comparator;    

import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.comment.Issue;

/**
* This class can be used to compare Comment objects
*/
public class CommentComparator extends StringComparator
{
	//types of Comment comparison
	public static final int DATE = 0;
	public static final int CREATOR = 1;
	public static final int COMMENT_ID = 2;
	public static final int SEGMENT_NUMBER = 3;


	/**
	* Creates a CommentComparator with the given locale.
	*/
	public CommentComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	 * Performs a comparison of two Comment objects by their
         * create date.  
	 */
	public int compare(java.lang.Object p_A, java.lang.Object p_B) 
        {
	    Comment a = (Comment) p_A;
	    Comment b = (Comment) p_B;

	    int rv;
        switch (m_type)
	    {
	        default:
            case DATE:
                Date aDate = a.getCreatedDateAsDate();
                Date bDate = b.getCreatedDateAsDate();
                if (aDate.after(bDate))
                {
                    rv = 1;
                }
                else if (aDate.equals(bDate))
                {
                    rv = 0;
                }
                else
                {
                    rv = -1;
                }
                break;
            case CREATOR:
                String aVal = a.getCreatorId();
                String bVal = b.getCreatorId();
                rv = aVal.compareTo(bVal);
                break;
            case COMMENT_ID:
                Long aId = new Long(a.getId());
                Long bId = new Long(b.getId());
                rv = aId.compareTo(bId);
                break;
            case SEGMENT_NUMBER:
            	String aSeg = getSegmentIdFromLogicalKey(((Issue)a).getLogicalKey());
            	String bSeg = getSegmentIdFromLogicalKey(((Issue)b).getLogicalKey());
            	rv = aSeg.compareTo(bSeg);
            	break;
	    }
        return rv;
	}
	
	public static String getSegmentIdFromLogicalKey(String logicalKey) {
		StringTokenizer tok = new StringTokenizer(logicalKey, "_");
		tok.nextToken();
		String segmentId = tok.nextToken();
		return segmentId;
	}
}
