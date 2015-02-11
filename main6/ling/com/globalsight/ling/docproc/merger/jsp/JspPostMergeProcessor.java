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
package com.globalsight.ling.docproc.merger.jsp;

import org.apache.log4j.Logger;

import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.ling.docproc.merger.plaintext.PlaintextPostMergeProcessor;

/**
 * This class post processes a merged JSP document.
 */
public class JspPostMergeProcessor
    implements PostMergeProcessor
{
    private static final String PROCESS_ERROR = "JSP post merge process error";
    private static Logger theLogger = Logger
            .getLogger(PlaintextPostMergeProcessor.class);
    private boolean addAdditionalHead = false;
    /**
     * @see com.globalsight.ling.document.merger.PostMergeProcessor#process(java.lang.String, java.lang.String)
     */
    public String process(String p_content, String p_IanaEncoding)
        throws DiplomatMergerException
    {
        String processed = null;
        RegExMatchInterface match = null;
        
        try
        {
            match =
                RegEx.matchSubstring(p_content, "(<%@\\s*page|<jsp:directive.page)[^>]+contentType\\s*=\\s*(['\"])([^'\"=]+)(charset\\s*=\\s*([^>'\" ]+))?\\s*\\2", false);
        }
        catch (RegExException e)
        {
            // Shouldn't happen
            theLogger.error(PROCESS_ERROR, e);
        }

        if(match == null)
        {
            if(addAdditionalHead)
            {
                processed = "<%@ page contentType=\"text/html; charset="
                    + p_IanaEncoding + "\" %>" + p_content;
            }
            else
            {
                processed = p_content; //GBS-741, remove the extra info above
            }
        }
        else
        {
            int charsetValueBegin = match.beginOffset(5);
            if(charsetValueBegin == -1)
            {
                int contentTypeEnd = match.endOffset(3);
                processed = p_content.substring(0, contentTypeEnd)
                    + "; charset=" + p_IanaEncoding
                    + p_content.substring(contentTypeEnd);
            }
            else
            {
                int charsetValueEnd = match.endOffset(5);
                processed = p_content.substring(0, charsetValueBegin)
                    + p_IanaEncoding + p_content.substring(charsetValueEnd);
            }
        }
        return processed;
    }

    public void setAddAdditionalHead(boolean addAdditionalHead)
    {
        this.addAdditionalHead = addAdditionalHead;
    }
}
