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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Abstract base class for managing "databases" of name-value
 * properties in a generic, text-based way.
 */
public abstract class PropertiesDatabase
{
    private String fileName;
    private Properties props;

    public PropertiesDatabase()
    {
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String string)
    {
        fileName = string;
    }

    protected Properties getProps()
    {
        return props;
    }

    protected void setProps(Properties properties)
    {
        props = properties;
    }

    protected void load()
        throws FileNotFoundException, IOException
    {
        InputStream is = new FileInputStream(fileName);
        props = new Properties();
        props.load(is);
        is.close();
    }

    protected void store()
        throws IOException
    {
        OutputStream os = new FileOutputStream(fileName);
        props.store(os, "");
        os.close();
    }
}
