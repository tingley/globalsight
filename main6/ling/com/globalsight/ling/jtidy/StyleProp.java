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
package com.globalsight.ling.jtidy;

/**
 * Linked list of style properties.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class StyleProp
{

    /**
     * Style name.
     */
    protected String name;

    /**
     * Style value.
     */
    protected String value;

    /**
     * Next linked style property.
     */
    protected StyleProp next;

    /**
     * Instantiates a new style property.
     * @param name Style name
     * @param value Style value
     * @param next Next linked style property. Can be null.
     */
    public StyleProp(String name, String value, StyleProp next)
    {
        this.name = name;
        this.value = value;
        this.next = next;
    }

}
