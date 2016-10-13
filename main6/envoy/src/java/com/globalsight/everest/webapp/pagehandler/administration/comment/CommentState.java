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

package com.globalsight.everest.webapp.pagehandler.administration.comment;

import com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants;
import com.globalsight.util.GlobalSightLocale;

import java.util.ArrayList;

/**
 * <p>A helper class that combines all state variables necessary for
 * the Comment Reference page handler to do his work into one object.</p>
 *
 * @see CommentConstants
 */
public class CommentState
    implements CommentConstants
{
    //
    // Member Variables
    //
    GlobalSightLocale m_targetLocale = null;

    // message when upload succeeds or fails
    String m_message = null;

    /**
     * Sorted list of CommentFile objects.
     */
    private ArrayList m_commentReferences;

    //
    // Constructors
    //

    public CommentState()
    {
    }

    //
    // Public methods
    //

    public void setCommentReferences(ArrayList p_list)
    {
        m_commentReferences = p_list;
    }

    public ArrayList getCommentReferences()
    {
        return m_commentReferences;
    }

    public void setTargetLocale(GlobalSightLocale p_locale)
    {
        m_targetLocale = p_locale;
    }

    public GlobalSightLocale getTargetLocale()
    {
        return m_targetLocale;
    }

    public void setMessage(String p_msg)
    {
        m_message = p_msg;
    }

    public String getMessage()
    {
        return m_message;
    }

}
