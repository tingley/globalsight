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
package com.globalsight.everest.servlet.util;

import java.util.Hashtable;
import javax.servlet.http.HttpSession;

/**
 * AppletDirectory is responsible for keeping a reference to the
 * HttpSession for an applet.  The applet can get the session by
 * passing a valid uid to the directory.
 *
 * @author Tomy A. Doomany
 */
public class AppletDirectory
{
    // singleton instance
    private static final AppletDirectory m_instance = new AppletDirectory();
    private Hashtable hashTable;

    //
    // Constructor
    //

    /**
     * AppletDirectory private constructor.
     */
    private AppletDirectory()
    {
        hashTable = new Hashtable();
    }

    /**
     * Get the single instance of the AppletDirectory
     *
     * @return The single instance of the AppletDirectory.
     */
    public static AppletDirectory getInstance()
    {
        return m_instance;
    }


    /**
     * Store the session within this applet directory.
     * @param p_session - The http session to be stored.
     * @return The generated key used for retreiving this session.
     */
    public String setSession(HttpSession p_session)
    {
        // generate uid
        String key = new java.rmi.server.UID().toString();

        hashTable.put(key, p_session);

        return key;
    }


    /**
     * Get the http session based on the key.
     * @param p_key - The key used for session retrieval.
     * @return The http session (if exists).
     */
    public HttpSession getSession(String p_key)
    {
        return (HttpSession)hashTable.get(p_key);
    }



    /**
     * Remove the session from this directory.
     * @param p_key _ The key of the session to be removed.
     * @return True if the deletion process was successful.
     * Otherwise, returns false.
     */
    public boolean removeSession(String p_key)
    {
        if (hashTable.containsKey(p_key))
        {
            hashTable.remove(p_key);
            return true;
        }

        return false;
    }
}
