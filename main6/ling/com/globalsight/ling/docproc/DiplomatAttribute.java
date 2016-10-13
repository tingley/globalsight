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
package com.globalsight.ling.docproc;

import java.util.Properties;
import com.globalsight.ling.common.DiplomatNames;

public class DiplomatAttribute
{
    private String version = null;
    private String locale = null;
    private String dataType = null;
    private String targetEncoding = null;
    private int wordCount = 0;
    private int id = 1;
    private String targetLanguage = null;


    // Thu Jan 29 16:52:35 2004 CvdL: updated GXML version from 1.0 to
    // 2.0 because of addition of "i" attribute to <IT>, which is
    // needed to support split/merging of BPT/EPT/IT.
    public DiplomatAttribute()
    {
        version = "2.0";
    }


    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    public String getLocale()
    {
        return locale;
    }


    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getDataType()
    {
        return dataType;
    }


    public void setTargetEncoding(String targetEncoding)
    {
        this.targetEncoding = targetEncoding;
    }

    public String getTargetEncoding()
    {
        return targetEncoding;
    }


    public void addWordCount(int wordCount)
    {
        this.wordCount += wordCount;
    }

    public int getWordCount()
    {
        return wordCount;
    }


    public void incrementId()
    {
        id++;
    }

    public int getId()
    {
        return id;
    }

    public String getTargetLanguage()
    {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage)
    {
        this.targetLanguage = targetLanguage;
    }
    
    public Properties CreateProperties()
    {
        Properties attribs = new Properties();

        if (version != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.VERSION, version);
        }
        if (locale != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.LOCALE, locale);
        }
        if (dataType != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.DATATYPE, dataType);
        }
        if (targetEncoding != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.TARGETENCODING,
                targetEncoding);
        }
        if (wordCount != 0)
        {
            attribs.setProperty(DiplomatNames.Attribute.WORDCOUNT,
                Integer.toString(wordCount));
        }
        if (targetLanguage != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.TARGETLANGUAGE, targetLanguage);
        }        

        return attribs;
    }
}
