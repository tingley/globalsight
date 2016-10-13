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
package com.globalsight.util.gxml;

//java
import java.util.List;

/**
 * Root Element of a GXML file, representing the <diplomat> tag element.
 */
public class GxmlRootElement extends GxmlElement
{
    GxmlRootElement()
    {
        super(GxmlElement.GXML_ROOT,
                GxmlNames.GXML_ROOT);
    }
    
    /**
     * Construct a GxmlElement with the given type and name
     */
    protected GxmlRootElement(int p_type, String p_name)
    {
        super(p_type, p_name);
    }
}
