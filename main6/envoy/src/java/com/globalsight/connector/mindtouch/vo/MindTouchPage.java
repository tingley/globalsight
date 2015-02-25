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
package com.globalsight.connector.mindtouch.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.globalsight.everest.util.comparator.StringComparator;

/**
 * This represents one MindTouch page basic information.
 * 
 * @author YorkJin
 * @since version 8.6 on 2015 Jan.
 */
public class MindTouchPage
{
    long id = -1;
    String href = null;
    String uriUi = null;
    String title = null;
    String path = null;
    String dateCreated= null;

    long parentId = -1;
    List<MindTouchPage> subpages = null;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getHref()
    {
        return href;
    }

    public void setHref(String href)
    {
        this.href = href;
    }

    public String getUriUi()
    {
        return uriUi;
    }

    public void setUriUi(String uri_ui)
    {
        this.uriUi = uri_ui;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    public List<MindTouchPage> getSubpages()
    {
        return subpages;
    }

    public void setSubpages(List<MindTouchPage> subpages)
    {
        this.subpages = subpages;
    }

    public void addSubPage(MindTouchPage mtp)
    {
        if (subpages == null)
        {
            subpages = new ArrayList<MindTouchPage>();
        }

        subpages.add(mtp);
        sort();
    }

    public void setParentId(long parentId)
    {
        this.parentId = parentId;
    }

    public long getParentId()
    {
        return parentId;
    }

    public String toJSON()
    {
        StringBuilder result = new StringBuilder();
        if (title != null && title.trim().length() > 0)
        {
            title = title.replace("\"", "\\\"");
            title = title.replace("/", "\\/");
            String uri = uriUi.replace("\"", "\\\"").replace("/", "\\/");
            String key = id + "_" + uri;

            result.append("{");
            result.append("\"title\":\"").append(title).append("\", ");
            result.append("\"key\":\"").append(key).append("\", ");
            result.append("\"isFolder\":").append(false).append(", ");
            result.append("\"isLazy\":").append(false).append(", ");
            result.append("\"expand\":").append(false);

            if (subpages != null && subpages.size() > 0)
            {
                result.append(", \"children\":[");
                for (MindTouchPage page : subpages)
                {
                    result.append(page.toJSON()).append(", ");
                }
                result.delete(result.length() - 2, result.length());
                result.append("]");
            }
            System.out.print("");
            result.append("}");
        }

        return result.toString();
    }

    private void sort()
    {
        Collections.sort(subpages, new MindTouchPageComparator());
    }

    private class MindTouchPageComparator extends StringComparator
    {
        private static final long serialVersionUID = -2974346308403562806L;

        public MindTouchPageComparator()
        {
            super(Locale.ENGLISH);
        }

        public int compare(Object p_A, Object p_B)
        {
            MindTouchPage a = (MindTouchPage) p_A;
            MindTouchPage b = (MindTouchPage) p_B;

            return super.compareStrings(a.getTitle(), b.getTitle());
        }
    }
}
