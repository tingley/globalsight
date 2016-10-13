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

package galign.data;

import java.net.URL;
import java.io.Serializable;

/**
 * A data object holding information to logon to an GlobalSight Server.
 */
public class ServerLogin
    implements Serializable
{
    private String m_serverName;
    private int m_serverPort;
    private boolean m_useHttps;
    private String m_url;

    private String m_user;
    private String m_password;

    //
    // Constructor
    //

    public ServerLogin()
    {
    }

    //
    // Public Methods
    //

    public String getServerName()
    {
        return m_serverName;
    }

    public void setServerName(String p_arg)
    {
        m_serverName = p_arg;
    }

    public int getServerPort()
    {
        return m_serverPort;
    }

    public void setServerPort(int p_arg)
    {
        m_serverPort = p_arg;
    }

    public void setServerPort(String p_arg)
    {
        m_serverPort = Integer.parseInt(p_arg);
    }

    public boolean getUseHttps()
    {
        return m_useHttps;
    }

    public void setUseHttps(boolean p_arg)
    {
        m_useHttps = p_arg;
    }

    public String getUrl()
    {
        return m_url;
    }

    public void setUrl(String p_arg)
    {
        m_url = p_arg;
    }

    public String getFullURL()
    {
        StringBuffer result = new StringBuffer();

        if (m_useHttps)
        {
            result.append("https");
        }
        else
        {
            result.append("http");
        }

        result.append("://");
        result.append(m_serverName);
        result.append(":");
        result.append(String.valueOf(m_serverPort));
        result.append("//");
        result.append(m_url);

        return result.toString();
    }
}
