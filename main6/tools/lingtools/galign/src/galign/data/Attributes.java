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

import galign.data.Attribute;

import java.util.ArrayList;

/**
 * A list of user-defined attributes such as "Product Version".
 */
public class Attributes
	extends ArrayList
{
    //
    // Constructor
    //

    public Attributes()
    {
		super();
    }

    //
    // Public Methods
    //

    public Attributes addAttribute(String p_name, String p_value)
    {
		this.add(new Attribute(p_name, p_value));
		return this;
    }

    public Attribute getAttribute(String p_name)
    {
		for (int i = 0, max = this.size(); i < max; i++)
		{
			Attribute attr = (Attribute)this.get(i);
			
			if (p_name.equals(attr.getName()))
			{
				return attr;
			}
		}

		return null;
    }
}

