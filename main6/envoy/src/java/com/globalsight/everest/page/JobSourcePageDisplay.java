/**
 * Copyright 2009 Welocalize, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * <p>
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.globalsight.everest.page;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

public class JobSourcePageDisplay
{
    private SourcePage sourcePage;
    private String pageUrl;
    private String dataSourceName;
    private boolean isWordCountOverriden;
    private String sourceLink;
    private boolean isImageFile;
    private boolean isActiveFileProfile;
    private long uiFileProfileId;
    // GBS-4749
    private long blaiseEntryId;
    private String blaiseUploadState = "--";
    private String blaiseCompleteState = "--";
    private String blaiseUploadException = "";
    private String blaiseCompleteException = "";

    public JobSourcePageDisplay(SourcePage sourcePage)
    {
        this.sourcePage = sourcePage;
    }

    public SourcePage getSourcePage()
    {
        return sourcePage;
    }

    public String getPageUrl()
    {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl)
    {
        this.pageUrl = pageUrl;
    }

    public String getDataSourceName()
    {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName)
    {
        this.dataSourceName = dataSourceName;
    }

    public boolean getIsImageFile()
    {
        return isImageFile;
    }

    public void setImageFile(boolean isImageFile)
    {
        this.isImageFile = isImageFile;
    }

    public boolean getIsWordCountOverriden()
    {
        return isWordCountOverriden;
    }

    public void setWordCountOverriden(boolean isWordCountOverriden)
    {
        this.isWordCountOverriden = isWordCountOverriden;
    }

    public String getSourceLink()
    {
        return sourceLink;
    }

    public void setSourceLink(String sourceLink)
    {
        this.sourceLink = sourceLink;
    }

    public void setSourcePage(SourcePage sourcePage)
    {
        this.sourcePage = sourcePage;
    }

    public boolean getIsActiveFileProfile()
    {
        return isActiveFileProfile;
    }

    public void setActiveFileProfile(boolean isActiveFileProfile)
    {
        this.isActiveFileProfile = isActiveFileProfile;
    }

    public long getUiFileProfileId()
    {
        return uiFileProfileId;
    }

    public void setUiFileProfileId(long uiFileProfileId)
    {
        this.uiFileProfileId = uiFileProfileId;
    }

    public long getBlaiseEntryId()
    {
        return blaiseEntryId;
    }

    public void setBlaiseEntryId(long blaiseEntryId)
    {
        this.blaiseEntryId = blaiseEntryId;
    }

    public String getBlaiseUploadState()
    {
        return blaiseUploadState;
    }

    public void setBlaiseUploadState(String blaiseUploadState)
    {
        if (StringUtil.isNotEmpty(blaiseUploadState))
        {
            this.blaiseUploadState = blaiseUploadState;
        }
    }

    public String getBlaiseCompleteState()
    {
        return blaiseCompleteState;
    }

    public void setBlaiseCompleteState(String blaiseCompleteState)
    {
        if (StringUtil.isNotEmpty(blaiseCompleteState))
        {
            this.blaiseCompleteState = blaiseCompleteState;
        }
    }

    public String getBlaiseUploadException()
    {
        return blaiseUploadException;
    }

    public void setBlaiseUploadException(String blaiseUploadException)
    {
        if (StringUtil.isNotEmpty(blaiseUploadException))
        {
            this.blaiseUploadException = getBlaiseStackTrace(blaiseUploadException);
        }
    }

    public String getBlaiseCompleteException()
    {
        return blaiseCompleteException;
    }

    public void setBlaiseCompleteException(String blaiseCompleteException)
    {
        if (StringUtil.isNotEmpty(blaiseCompleteException))
        {
            this.blaiseCompleteException = getBlaiseStackTrace(blaiseCompleteException);
        }
    }

    @SuppressWarnings("unchecked")
    private String getBlaiseStackTrace(String exceptionXml)
    {
        String stackTrace = "";
        if (StringUtil.isNotEmpty(exceptionXml))
        {
            try
            {
                Document document = DocumentHelper.parseText(exceptionXml);
                Element root = document.getRootElement();
                List<Element> originalExceptionNodes = root
                        .elements(GeneralException.ORIGINAL_EXCEPTION);
                if (originalExceptionNodes != null && originalExceptionNodes.size() > 0)
                {
                    List<Element> stackTraceNodes = originalExceptionNodes.get(0)
                            .elements(GeneralException.STACKTRACE);
                    if (stackTraceNodes != null && stackTraceNodes.size() > 0)
                    {
                        stackTrace = stackTraceNodes.get(0).getText();
                        stackTrace = StringUtil.replace(stackTrace, "&gt;", ">");
                        stackTrace = StringUtil.replace(stackTrace, "&lt;", "<");
                        stackTrace = StringUtil.replace(stackTrace, "&#xd;", "");
                        stackTrace = StringUtil.replace(stackTrace, "\\\"", "\"");
                        stackTrace = StringUtil.replace(stackTrace, "\\r\\n", "");
                    }
                }
            }
            catch (DocumentException ignore)
            {
            }
        }
        return stackTrace;
    }
}
