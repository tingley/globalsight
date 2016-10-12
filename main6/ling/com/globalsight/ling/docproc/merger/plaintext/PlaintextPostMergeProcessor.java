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
package com.globalsight.ling.docproc.merger.plaintext;

import java.io.File;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

/**
 * This class post processes a merged plain text document.
 */
public class PlaintextPostMergeProcessor implements PostMergeProcessor
{
    // GBS-3830
    private int m_eolEncoding = 0;
    private String m_fileName = null;
    private static final String EOL_LF = "\n";
    private static final String EOL_CRLF = "\r\n";

    public void setEolEncoding(int p_eolEncoding)
    {
        m_eolEncoding = p_eolEncoding;
    }

    public void setFileName(String p_fileName)
    {
        m_fileName = p_fileName;
    }

    /**
     * @see com.globalsight.ling.document.merger.PostMergeProcessor#process(java.lang.String,
     *      java.lang.String)
     */
    public String process(String content, String ianaEncoding) throws DiplomatMergerException
    {
        // for GBS-3830
        if (m_fileName == null)
        {
            // m_fileName can not be null if this is from export request
            return content;
        }
        File docDir = AmbFileStoragePathUtils.getCxeDocDir();
        File sourceFile = new File(docDir, m_fileName);
        boolean isSourceCRLF = FileUtil.isWindowsReturnMethod(sourceFile.getAbsolutePath());

        if (m_eolEncoding == FileProfileImpl.EOL_ENCODING_PRESERVE && isSourceCRLF
                && content.indexOf(EOL_CRLF) == -1)
        {
            content = StringUtil.replace(content, EOL_LF, EOL_CRLF);
        }
        else if (m_eolEncoding == FileProfileImpl.EOL_ENCODING_CRLF
                && content.indexOf(EOL_CRLF) == -1)
        {
            content = StringUtil.replace(content, EOL_LF, EOL_CRLF);
        }

        return content;
    }
}
