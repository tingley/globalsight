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
// This software is in the public static domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/
// Version 2.0
// Changes from version 1.x:
package com.globalsight.ling.sgml.dtd2;

/**
 * Class representing a parameter entity.
 *
 * <p><b>WARNING!</b> This class is used during parsing. After parsing,
 * it is not guaranteed to contain useful information. In particular,
 * ParameterEntity objects cannot be used to reconstruct a DTD and
 * are not reference from where they were used, such as in content models.
 * This is because DTD and its related classes are designed to be used by
 * applications that want to explore the "logical" structure of a DTD --
 * that is, its element types, attributes, and notations -- rather than its
 * physical structure.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class ParameterEntity
    extends Entity
{
    // ********************************************************************
    // Variables
    // ********************************************************************

    /**
     * The value of the parameter entity. This also serves as a flag
     * that the entity is an internal entity. It must be null if the
     * systemID or publicID variables inherited from Entity are non-null.
     */
    public String value = null;

    // ********************************************************************
    // Constructors
    // ********************************************************************

    /** Construct a ParameterEntity. */
    public ParameterEntity()
    {
        this.type = Entity.TYPE_PARAMETER;
    }

    /** Construct a ParameterEntity and set its name. */
    public ParameterEntity(String name)
    {
        super(name);
        this.type = Entity.TYPE_PARAMETER;
    }

    public String getValue()
    {
        return value;
    }
}
