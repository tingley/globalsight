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
 * Linked list of class names and styles.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class Style
{

    /**
     * Tag name.
     */
    protected String tag;

    /**
     * Tag class.
     */
    protected String tagClass;

    /**
     * Style properties.
     */
    protected String properties;

    /**
     * Next linked style element.
     */
    protected Style next;

    /**
     * Instantiates a new style.
     * @param tag Tag name
     * @param tagClass Tag class
     * @param properties Style properties
     * @param next Next linked style element. Can be null.
     */
    public Style(String tag, String tagClass, String properties, Style next)
    {
        this.tag = tag;
        this.tagClass = tagClass;
        this.properties = properties;
        this.next = next;
    }

}
