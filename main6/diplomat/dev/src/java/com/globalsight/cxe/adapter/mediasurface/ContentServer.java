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
package com.globalsight.cxe.adapter.mediasurface;


/**
 * Represents a Mediasurface content server
 * description
 */
public class ContentServer
{

    /**
     * The content server URL (ex. iiop://10.0.0.145/Mediasurface)
     */
    public String url = null;

    /**
     * The content server name (ex. Staging Server)
     */
    public String name = null;

    /**
     * The content server port (ex. 8080)
     */
    public int port = 8080;

    /**
     * Constructs a ContentServer with null fields
     */
    public ContentServer(){}

    /**
     * Constructs a ContentServer with the given info
     * 
     * @param p_url
     * @param p_name
     * @param p_port
     */
    public ContentServer (String p_url, String p_name, int p_port)
    {
        url = p_url;
        name = p_name;
        port = p_port;
    }

    /**
     * Returns a string representation of the content server.
     * This can be used as a key for putting content servers
     * in Hashtables
     * 
     * @return String in the format
     *         <url>|<name>|port
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer(url);
        sb.append("|").append(name);
        sb.append("|").append(port);
        return sb.toString();
    }
}

