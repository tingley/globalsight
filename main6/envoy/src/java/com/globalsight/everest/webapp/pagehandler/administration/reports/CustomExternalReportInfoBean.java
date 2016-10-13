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
package com.globalsight.everest.webapp.pagehandler.administration.reports;
import java.util.Properties;

/**
 * Simple javabean to set/get some custom report information
 */
public class CustomExternalReportInfoBean
{
    private int m_number; //number of report: custom1, custom2, etc.
    private String m_url; //external URL
    private String m_desc; //report description
    private String m_name; //report name

    public CustomExternalReportInfoBean()
    {
    }

    /**
     * Creates a CustomExternalReportInfoBean with the basic information
     * 
     * @param p_number
     * @param p_url
     * @param p_name
     * @param p_desc
     */
    public CustomExternalReportInfoBean(int p_number, String p_url, String p_name, String p_desc)
    {
        m_number = p_number;
        m_url = p_url;
        m_name = p_name;
        m_desc = p_desc;
    }

    public int getNumber()
    {
        return m_number;
    }

    public String getName()
    {
        return m_name;
    }

    public String getUrl()
    {
        return m_url;
    }

    public String getDesc()
    {
        return m_desc;
    }

    /**
     * Returns a string containing number,name,and URL
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("(number=").append(m_number).append(",name=");
        sb.append(m_name).append(",url=").append(m_url);
        sb.append(")");
        return sb.toString();
    }
}

