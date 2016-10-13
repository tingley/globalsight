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

import java.util.*;

/**
 * Class representing an attribute.
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class Attribute
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Attribute type unknown. */
   public static final int TYPE_UNKNOWN = 0;

   /** Attribute type CDATA. */
   public static final int TYPE_CDATA = 1;

   /** Attribute type ID. */
   public static final int TYPE_ID = 2;

   /** Attribute type IDREF. */
   public static final int TYPE_IDREF = 3;

   /** Attribute type IDREFS. */
   public static final int TYPE_IDREFS = 4;

   /** Attribute type ENTITY. */
   public static final int TYPE_ENTITY = 5;

   /** Attribute type ENTITIES. */
   public static final int TYPE_ENTITIES = 6;

   /** Attribute type NMTOKEN. */
   public static final int TYPE_NMTOKEN = 7;

   /** Attribute type NMTOKENS. */
   public static final int TYPE_NMTOKENS = 8;

   /** Enumerated attribute type. */
   public static final int TYPE_ENUMERATED = 9;

   /** Notation attribute type. */
   public static final int TYPE_NOTATION = 10;

   /** Default type unknown. */
   public static final int REQUIRED_UNKNOWN = 0;

   /** Attribute is required, no default. Corresponds to #REQUIRED. */
   public static final int REQUIRED_REQUIRED = 1;

   /** Attribute is optional, no default. Corresponds to #IMPLIED. */
   public static final int REQUIRED_OPTIONAL = 2;

   /** Attribute has a fixed default. Corresponds to #FIXED "&lt;default>". */
   public static final int REQUIRED_FIXED = 3;

   /** Attribute is optional and has a default. Corresponds to "&lt;default>". */
   public static final int REQUIRED_DEFAULT = 4;

   // ********************************************************************
   // Variables
   // ********************************************************************

   /** The XMLName of the attribute. */
   public XMLName name = null;

   /** The attribute type. */
   public int type = TYPE_UNKNOWN;

   /** Whether the attribute is required and has a default. */
   public int required = REQUIRED_UNKNOWN;

   /** The attribute's default value. May be null. */
   public String defaultValue = null;

   /**
    * The legal values for attributes with a type of TYPE_ENUMERATED or
    * TYPE_NOTATION. Otherwise null.
    */
   public Vector enums = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new Attribute. */
   public Attribute()
   {
   }

   /**
    * Construct a new Attribute from its namespace URI, local name, and prefix.
    *
    * @param uri Namespace URI of the attribute. May be null.
    * @param localName Local name of the attribute.
    * @param prefix Namespace prefix of the attribute. May be null.
    */
   public Attribute(String uri, String localName, String prefix)
   {
      name = XMLName.create(uri, localName, prefix);
   }

   /**
    * Construct a new Attribute from an XMLName.
    *
    * @param name XMLName of the attribute.
    */
   public Attribute(XMLName name)
   {
      this.name = name;
   }
}
