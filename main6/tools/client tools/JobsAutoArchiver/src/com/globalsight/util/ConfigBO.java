/**
 *  Copyright 2014 Welocalize, Inc. 
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
package com.globalsight.util;

/**
 * The Configuration Business Object.
 */
public class ConfigBO
{
    String hostName;
    int port = 80;
    boolean isHttpsEnabled = false;
    String userName;
    String password;

    int intervalTimeForArchive;
    int intervalTime;

    public ConfigBO(String hostName, int port, String userName, String password)
    {
        this(hostName, port, userName, password, false);
    }

    public ConfigBO(String hostName, int port, String userName,
            String password, int intervalTimeForArchive, int intervalTime)
    {
        this(hostName, port, userName, password, intervalTimeForArchive,
                intervalTime, false);
    }

    public ConfigBO(String hostName, int port, String userName,
            String password, boolean isHttpsEnabled)
    {
        this.hostName = hostName;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.isHttpsEnabled = isHttpsEnabled;
    }

    public ConfigBO(String hostName, int port, String userName,
            String password, int intervalTimeForArchive, int intervalTime,
            boolean isHttpsEnabled)
    {
        this.hostName = hostName;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.intervalTimeForArchive = intervalTimeForArchive;
        this.intervalTime = intervalTime;
        this.isHttpsEnabled = isHttpsEnabled;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public int getIntervalTimeForArchive()
    {
        return intervalTimeForArchive;
    }

    public void setIntervalTimeForArchive(int intervalTimeForArchive)
    {
        this.intervalTimeForArchive = intervalTimeForArchive;
    }

    public int getIntervalTime()
    {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime)
    {
        this.intervalTime = intervalTime;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean isHttpsEnabled()
    {
        return isHttpsEnabled;
    }

    public void setUseHTTPS(boolean isUseHTTPS)
    {
        this.isHttpsEnabled = isUseHTTPS;
    }
}
