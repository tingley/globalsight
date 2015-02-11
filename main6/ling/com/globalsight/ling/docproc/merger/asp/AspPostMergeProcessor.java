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
package com.globalsight.ling.docproc.merger.asp;

import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;

import com.globalsight.diplomat.util.Logger;

/**
 * This class post processes a merged ASP document.
 */
public class AspPostMergeProcessor
    implements PostMergeProcessor
{
    private static final String PROCESS_ERROR = "ASP post merge process error";
    private static Logger theLogger = Logger.getLogger();

    private String m_content = null;
    
    /**
     * @see com.globalsight.ling.document.merger.PostMergeProcessor#process(java.lang.String, java.lang.String)
     */
    public String process(String p_content, String p_IanaEncoding)
        throws DiplomatMergerException
    {
        m_content = p_content;

        if(!rewriteContentType(p_IanaEncoding))
        {
            rewriteCharset(p_IanaEncoding);
        }

        return m_content;
    }
    

    private boolean rewriteContentType(String p_encoding)
    {
        boolean processed = true;
        RegExMatchInterface match = null;
        
        try
        {
            match =
                RegEx.matchSubstring(m_content, "<%[^>]*Response.ContentType\\s*=\\s*(['\"])[^'\"=]+charset\\s*=\\s*([^>'\" ]+)\\s*\\1", false);
        }
        catch (RegExException e)
        {
            // Shouldn't happen
            theLogger.printStackTrace(Logger.ERROR, PROCESS_ERROR, e);
        }

        if(match == null)
        {
            processed = false;
        }
        else
        {
            int charsetValueBegin = match.beginOffset(2);
            int charsetValueEnd = match.endOffset(2);
            m_content = m_content.substring(0, charsetValueBegin)
                + p_encoding + m_content.substring(charsetValueEnd);
            processed = true;
        }

        return processed;
    }



    private void rewriteCharset(String p_encoding)
        throws DiplomatMergerException
    {
        RegExMatchInterface match = null;
        
        try
        {
            match =
                RegEx.matchSubstring(m_content, "<%[^>]*Response.Charset\\s*=\\s*(['\"])([^>'\" ]+)\\s*\\1", false);
        }
        catch (RegExException e)
        {
            // Shouldn't happen
            theLogger.printStackTrace(Logger.ERROR, PROCESS_ERROR, e);
        }

        if(match == null)
        {
            addCharset(p_encoding);
        }
        else
        {
            int charsetValueBegin = match.beginOffset(2);
            int charsetValueEnd = match.endOffset(2);
            m_content = m_content.substring(0, charsetValueBegin)
                + p_encoding + m_content.substring(charsetValueEnd);
        }
    }


    private void addCharset(String p_encoding)
        throws DiplomatMergerException
    {
        RegExMatchInterface match = null;
        
        try
        {
            // look for the begining of the ASP directive
            match = RegEx.matchSubstring(m_content, "^\\s*<%\\s*@");
        }
        catch (RegExException e)
        {
            // Shouldn't happen
            theLogger.printStackTrace(Logger.ERROR, PROCESS_ERROR, e);
        }

        if(match == null)
        {
            m_content = "<% Response.Charset = \"" + p_encoding
                + "\" %>\n" + m_content;
        }
        else
        {
            // look for the end of the ASP directive
            int index = m_content.indexOf("%>");
            if(index == -1)
            {
                DiplomatMergerException e =
                    new DiplomatMergerException("AspIllFormed", null, null);
                theLogger.printStackTrace(Logger.ERROR, PROCESS_ERROR, e);
                throw e;
            }

            // forward the index to the end of the ASP directive
            index += 2;
            
            m_content = m_content.substring(0, index)
                + "\n<% Response.Charset = \"" + p_encoding
                + "\" %>" + m_content.substring(index);
        }
    }
}
