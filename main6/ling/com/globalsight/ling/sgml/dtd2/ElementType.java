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
 * Class representing an element type.
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class ElementType
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Unknown content type. */
   public static final int CONTENT_UNKNOWN = 0;

   /** Empty content type. */
   public static final int CONTENT_EMPTY = 1;

   /** Any content type. */
   public static final int CONTENT_ANY = 2;

   /** PCDATA-only content type. */
   public static final int CONTENT_PCDATA = 3;

   /**
    * "Mixed" content type.
    *
    * <p>The content model must include at least one child element type.</p>
    */
   public static final int CONTENT_MIXED = 4;

   /** Element content type. */
   public static final int CONTENT_ELEMENT = 5;

   // ********************************************************************
   // Variables
   // ********************************************************************

   /** The XMLName of the element type. */
   public XMLName name;

   /**
    * The type of the content model.
    *
    * <p> This must be one of the CONTENT_* constants. The default
    * is CONTENT_UNKNOWN.</p>
    */
   public int contentType = CONTENT_UNKNOWN;

   /**
    * A Group representing the content model.
    *
    * <p>Must be null if the content type is not CONTENT_ELEMENT or CONTENT_MIXED.
    * In the latter case, it must be a choice group with no child Groups.</p>
    */
   public Group content = null;

   /**
    * A Hashtable of Attributes.
    *
    * <p>Keyed by the attribute's XMLName. May be empty.</p>
    */
   public Hashtable attributes = new Hashtable();

   /**
    * A Hashtable of child ElementTypes.
    *
    * <p>Keyed by the child's XMLName. May be empty.</p>
    */
   public Hashtable children = new Hashtable();

   /**
    * A Hashtable of parent ElementTypes.
    *
    * <p>Keyed by the parent's XMLName. May be empty.</p>
    */
   public Hashtable parents = new Hashtable();

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new ElementType. */
   public ElementType()
   {
   }

   /**
    * Construct a new ElementType from its namespace URI, local name, and prefix.
    *
    * @param uri Namespace URI of the element type. May be null.
    * @param localName Local name of the element type.
    * @param prefix Namespace prefix of the element type. May be null.
    */
   public ElementType(String uri, String localName, String prefix)
   {
      name = XMLName.create(uri, localName, prefix);
   }

   /**
    * Construct a new ElementType from an XMLName.
    *
    * @param name XMLName of the element type.
    */
   public ElementType(XMLName name)
   {
      this.name = name;
   }
}
