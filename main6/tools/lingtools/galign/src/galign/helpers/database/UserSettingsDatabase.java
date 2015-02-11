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

package galign.helpers.database;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Properties;

/**
 * Manages a "database" of user options (name-value properties) in a
 * Java properties file.
 *
 * The database gets read automatically on application startup and
 * gets saved when the application exits.
 */
public class UserSettingsDatabase
    extends PropertiesDatabase
{
    private static final String PROP_CURRENT_DIRECTORY = "current_directory";
    private static final String PROP_RECENTLY_USED = "recently_used";

    private String m_currentDirectory;
    private LinkedList m_recentlyUsed;

    public UserSettingsDatabase()
    {
    }

    public void load()
        throws FileNotFoundException, IOException
    {
        super.load();
        Properties props = getProps();

        m_currentDirectory = props.getProperty(PROP_CURRENT_DIRECTORY, "").trim();
        m_recentlyUsed = initRecentlyUsed(props);
    }

    public synchronized String getCurrentDirectory()
    {
        return m_currentDirectory;
    }

    public synchronized void setCurrentDirectory(String p_cwd)
    {
        m_currentDirectory = p_cwd;
    }

    public synchronized void updateRecentlyUsed(String last)
    {
        if (m_recentlyUsed.contains(last))
        {
            m_recentlyUsed.remove(last);
        }
        m_recentlyUsed.addFirst(last);
        try
        {
            store();
        }
        catch (Exception e)
        {
        }
    }

    public synchronized LinkedList getRecentlyUsed()
    {
        return m_recentlyUsed;
    }

    public void store()
        throws IOException
    {
        Properties props = new Properties();

        props.setProperty(PROP_CURRENT_DIRECTORY, m_currentDirectory);
    
        ListIterator li = m_recentlyUsed.listIterator(0);
        int i = 0;
        while (li.hasNext())
        {
            props.setProperty(PROP_RECENTLY_USED + i++, (String)li.next());
        }

        setProps(props);

        super.store();
    }
    
    private LinkedList initRecentlyUsed(Properties props)
    {
        m_recentlyUsed = new LinkedList();
        for (int i = 0; i < 5; i++)
        {
            String used = props.getProperty(PROP_RECENTLY_USED + i, "");

            if (used == null || used.length() == 0) break; 
            m_recentlyUsed.add(used);
        }
        return m_recentlyUsed;
    }
}
