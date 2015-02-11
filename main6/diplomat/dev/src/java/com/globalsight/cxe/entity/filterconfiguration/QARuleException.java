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
package com.globalsight.cxe.entity.filterconfiguration;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GlobalSightLocale;

public class QARuleException
{
    private static final Logger logger = Logger
            .getLogger(QARuleException.class);

    private String exceptionContent = null;
    private boolean exceptionIsRE = false;
    // localeId
    private long language = -1;

    public QARuleException()
    {
    }

    public QARuleException(String exceptionContent, boolean exceptionIsRE,
            long language)
    {
        this.exceptionContent = exceptionContent;
        this.exceptionIsRE = exceptionIsRE;
        this.language = language;
    }

    public static QARuleException initFromElement(Element tagElement)
    {
        Node exceptionContentElement = tagElement.getElementsByTagName(
                "exceptionContent").item(0);
        String exceptionContent = exceptionContentElement.getFirstChild()
                .getNodeValue();

        boolean exceptionIsRE = false;
        Node isREElement = tagElement.getElementsByTagName("exceptionIsRE")
                .item(0);
        exceptionIsRE = "true".equals(isREElement.getFirstChild()
                .getNodeValue());

        Node languageElement = tagElement.getElementsByTagName("language")
                .item(0);
        String language = languageElement.getFirstChild().getNodeValue();

        QARuleException exception = new QARuleException(exceptionContent,
                exceptionIsRE, Long.parseLong(language));

        return exception;
    }

    public String getExceptionContent()
    {
        return exceptionContent;
    }

    public void setExceptionContent(String exceptionContent)
    {
        this.exceptionContent = exceptionContent;
    }

    public boolean exceptionIsRE()
    {
        return exceptionIsRE;
    }

    public void setExceptionIsRE(boolean exceptionIsRE)
    {
        this.exceptionIsRE = exceptionIsRE;
    }

    public long getLanguage()
    {
        return language;
    }

    public void setLanguage(long language)
    {
        this.language = language;
    }

    private String getLocaleCode()
    {
        long localeId = this.language;
        try
        {
            GlobalSightLocale locale = ServerProxy.getLocaleManager()
                    .getLocaleById(localeId);

            return locale.getLocaleCode();
        }
        catch (Exception e)
        {
            logger.error("An error occurred while getting locale with id: "
                    + localeId, e);
        }
        return "";
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("exceptionContent=");
        sb.append(this.exceptionContent);
        sb.append("|exceptionIsRE=");
        sb.append(this.exceptionIsRE);
        sb.append("|language=");
        sb.append(getLocaleCode());

        return sb.toString();
    }
}
