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
package com.globalsight.everest.webapp.pagehandler.rss;

import java.util.HashSet;
import java.util.Set;

import com.globalsight.everest.persistence.PersistentObject;

public class Feed extends PersistentObject
{
    private static final long serialVersionUID = -7604953142681546189L;

    private String rssUrl = null;
    private String rssEncoding = null;
    private String rssVersion = "2.0";

    private String channelTitle = null;
    private String channelLink = null;
    private String channelDescription = null;

    private String channelLanguage = null;
    private String channelCopyRight = null;

    private String imageTitle = null;
    private String imageLink = null;
    private String imageUrl = null;

    private boolean isDefault = false;
    private long companyId = -1;

    // RSS <item> data (Item object)
    private Set items = null;

    public Feed()
    {

    }

    public Feed(String rssUrl, String rssEncoding, String rssVersion,
            String channelTitle, String channelLink, String channelDescription,
            String channelLanguage, String channelCopyRight, String imageTitle,
            String imageLink, String imageUrl, boolean isDefault,
            String companyId)
    {
        this.rssUrl = rssUrl;
        this.rssEncoding = rssEncoding;
        this.rssVersion = rssVersion;
        this.channelTitle = channelTitle;
        this.channelLink = channelLink;
        this.channelDescription = channelDescription;
        this.channelLanguage = channelLanguage;
        this.channelCopyRight = channelCopyRight;
        this.imageTitle = imageTitle;
        this.imageLink = imageLink;
        this.imageUrl = imageUrl;
        this.isDefault = isDefault;
        this.companyId = Long.parseLong(companyId);
    }

    public String getRssUrl()
    {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl)
    {
        this.rssUrl = rssUrl;
    }

    public String getRssEncoding()
    {
        return rssEncoding;
    }

    public void setRssEncoding(String rssEncoding)
    {
        this.rssEncoding = rssEncoding;
    }

    public String getRssVersion()
    {
        return rssVersion;
    }

    public void setRssVersion(String rssVersion)
    {
        this.rssVersion = rssVersion;
    }

    public String getChannelTitle()
    {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle)
    {
        this.channelTitle = channelTitle;
    }

    public String getChannelLink()
    {
        return channelLink;
    }

    public void setChannelLink(String channelLink)
    {
        this.channelLink = channelLink;
    }

    public String getChannelDescription()
    {
        return channelDescription;
    }

    public void setChannelDescription(String channelDescription)
    {
        this.channelDescription = channelDescription;
    }

    public String getChannelLanguage()
    {
        return channelLanguage;
    }

    public void setChannelLanguage(String channelLanguage)
    {
        this.channelLanguage = channelLanguage;
    }

    public String getChannelCopyRight()
    {
        return channelCopyRight;
    }

    public void setChannelCopyRight(String channelCopyRight)
    {
        this.channelCopyRight = channelCopyRight;
    }

    public Set getItems()
    {
        return items;
    }

    public void setItems(Set items)
    {
        this.items = items;
    }

    public void addItem(Item item)
    {
        if (this.items == null)
        {
            this.items = new HashSet();
        }

        this.items.add(item);
    }

    public void removeItem(Item item)
    {
        if (this.items != null)
        {
            this.items.remove(item);
        }
    }

    public String getImageTitle()
    {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle)
    {
        this.imageTitle = imageTitle;
    }

    public String getImageLink()
    {
        return imageLink;
    }

    public void setImageLink(String imageLink)
    {
        this.imageLink = imageLink;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public boolean getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

}
