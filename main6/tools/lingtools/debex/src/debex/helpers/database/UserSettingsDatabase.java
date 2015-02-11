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

package debex.helpers.database;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class UserSettingsDatabase
    extends PropertiesDatabase
{
    private static final String PROP_CURRENT_DIRECTORY = "current_directory";

    private String m_currentDirectory;

    public UserSettingsDatabase()
    {
    }

    public void load()
        throws FileNotFoundException, IOException
    {
        super.load();
        Properties props = getProps();

        m_currentDirectory = props.getProperty(PROP_CURRENT_DIRECTORY, "").trim();
    }

    public synchronized String getCurrentDirectory()
    {
        return m_currentDirectory;
    }

    public synchronized void setCurrentDirectory(String p_cwd)
    {
        m_currentDirectory = p_cwd;
    }

    public void store()
        throws IOException
    {
        Properties props = new Properties();

        props.setProperty(PROP_CURRENT_DIRECTORY, m_currentDirectory);

        setProps(props);

        super.store();
    }
}
