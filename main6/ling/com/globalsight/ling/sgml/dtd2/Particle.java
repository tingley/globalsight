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
 * Class representing a content particle in a content model.
 *
 * <p>This is the base class for Group and Reference.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class Particle
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Content particle type unknown. */
   public static final int TYPE_UNKNOWN = 0;

   /** Content particle is a reference to an element type (Reference). */
   public static final int TYPE_ELEMENTTYPEREF = 1;

   /** Content particle is a choice group (Group). */
   public static final int TYPE_CHOICE = 2;

   /** Content particle is a sequence group (Group). */
   public static final int TYPE_SEQUENCE = 3;

   // ********************************************************************
   // Variables
   // ********************************************************************

   /** Content particle type. */
   public int     type = TYPE_UNKNOWN;

   /**
    * Whether the particle is required.
    *
    * <p>By default, this is true. The following table shows how isRequired
    * and isRepeatable map to the *, +, and ? qualifiers:</p>
    *
    * <pre>
    *
    *                         isRequired
    *                   ------------------------
    *    isRepeatable  |   true    |    false
    *    --------------|-----------|------------
    *            true  |     +     |      *
    *    --------------|-----------|------------
    *           false  |     --    |      ?
    *
    * </pre>
    *
    * <p>Note that the defaults of isRequired and isRepeatable map to
    * the required/not repeatable (i.e. no operator) case.</p>
    */
   public boolean isRequired = true;

   /** Whether the particle may be repeated.
    *
    * <p>By default, this is false.</p>
    */
   public boolean isRepeatable = false;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new Particle. */
   public Particle()
   {
   }
}
