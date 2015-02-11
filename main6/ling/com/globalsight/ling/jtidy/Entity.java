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
 * HTML ISO entity.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class Entity
{

    /**
     * entity name.
     */
    private String name;

    /**
     * entity code.
     */
    private short code;

    /**
     * instantiates a new entity.
     * @param name entity name
     * @param code entity code (will be casted to short)
     */
    public Entity(String name, int code)
    {
        this.name = name;
        this.code = (short) code;
    }

    /**
     * Getter for <code>code</code>.
     * @return Returns the code.
     */
    public short getCode()
    {
        return this.code;
    }

    /**
     * Getter for <code>name</code>.
     * @return Returns the name.
     */
    public String getName()
    {
        return this.name;
    }
}
