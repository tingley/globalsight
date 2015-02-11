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

/**
 * A user-defined attribute such as "Product Version".
 */
public class Attribute
{
    //
    // Members
    //
    private String m_name;
    private String m_value;

    //
    // Constructor
    //
    public Attribute()
    {
    }

    public Attribute(String p_name)
    {
        m_name = p_name;
    }

    public Attribute(String p_name, String p_value)
    {
        m_name = p_name;
        m_value = p_value;
    }

    //
    // Public Methods
    //

    public String getName()
    {
        return m_name;
    }

    public String getValue()
    {
        return m_value;
    }
}

